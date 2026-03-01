package ru.mammoth70.totpgenerator

import java.util.regex.Pattern
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Base64
import java.io.ByteArrayOutputStream
import androidx.annotation.VisibleForTesting
import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import org.apache.commons.codec.binary.Base32

// Парсер разбора строки схем otpauth://totp и otpauth-migration://offline
// Конвертор списка auths в url схемы otpauth-migration://offline

private const val REGEXP_HEAD1 = "^otpauth://totp/(\\S+?)\\?"
private const val REGEXP_HEAD2 = "^otpauth-migration://offline\\?data=(\\S+?)$"
private val patternHead1 = Pattern.compile(REGEXP_HEAD1)
private val patternHead2 = Pattern.compile(REGEXP_HEAD2)

private enum class OtpAlgorithm(val id: Int) {
    UNSPECIFIED(0), SHA1(1), SHA256(2), SHA512(3), MD5(4);
    companion object {
        fun fromId(id: Int) = entries.find { it.id == id } ?: SHA1
    }
}

private enum class DigitCount(val id: Int) {
    UNSPECIFIED(0), SIX(1), EIGHT(2), SEVEN(3); // (SEVEN - нестандартный ID)
    companion object {
        fun fromId(id: Int) = entries.find { it.id == id } ?: SIX
    }
}

private enum class OtpType(val id: Int) {
    UNSPECIFIED(0), HOTP(1), TOTP(2);
    companion object {
        fun fromId(id: Int) = entries.find { it.id == id } ?: TOTP
    }
}


fun parseQR(url: String?): List<OTPauth> {
    // Функция разбирает строку url.
    // В зависимости от результатов предварительного разбора,
    // вызывает функции parseOTPauth или parseGoogleMigration.

    val auths = mutableListOf<OTPauth>()
    if (url.isNullOrBlank()) {
        return auths
    }

    val matcher1 = patternHead1.matcher(url)
    val matcher2 = patternHead2.matcher(url)
    if ((matcher1.find())) {
        parseOTPauth(url)?.let { auths.add(it) }
    } else if ((matcher2.find())) {
        parseGoogleMigration(url).forEach { auths.add(it) }
    }
    return auths
}


@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun parseOTPauth(url: String): OTPauth? {
// Функция разбирает строку url otpauth://totp и в случае удачи возвращат OTPauth, в противном случае - null.

    return try {
        val uri = URI(url)

        // Разбираем query-параметры вручную (т.к. у java.net.URI нет getQueryParameter).
        val queryParams = uri.query?.split("&")?.associate {
            val parts = it.split("=", limit = 2)
            val key = parts[0]
            val value = if (parts.size > 1) URLDecoder.decode(parts[1], "UTF-8") else ""
            key to value
        } ?: emptyMap()

        // Извлекаем секрет.
        val secret = queryParams["secret"]?.uppercase() ?: return null
        if (!isValidBase32(secret)) return null

        // Извлекаем label (из пути) и issuer.
        var label = uri.path?.removePrefix("/") ?: ""
        val issuer = queryParams["issuer"] ?: ""

        // Google часто значение issuer хранит в начале поля label через двоеточие.
        if (issuer.isNotBlank() && label.startsWith("$issuer:", ignoreCase = true)) {
            label = label.substring(issuer.length + 1).trim()
        }

        OTPauth(
            label = label,
            secret = secret,
            issuer = issuer,
            period = queryParams["period"]?.toIntOrNull() ?: DEFAULT_PERIOD,
            hash = queryParams["algorithm"]?.uppercase() ?: SHA1,
            digits = queryParams["digits"]?.toIntOrNull() ?: DEFAULT_DIGITS,
        )
    } catch (e: Exception) {
        LogSmart.e("OTPauthUriParser", "Exception в parseOTPauth($url)", e)
        null // Если URL совсем кривой
    }
}


@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun parseGoogleMigration(url: String): List<OTPauth> {
    // Функция разбирает строку url otpauth-migration://offline,
    // и в случае удачи возвращат список OTPauth, в противном случае - список возвращается пустым.

    val auths = mutableListOf<OTPauth>()
    val pattern2 = Pattern.compile(REGEXP_HEAD2)
    val matcher2 = pattern2.matcher(url)
    if ((matcher2.find()) && (!matcher2.group(1).isNullOrBlank())) {
        val data = matcher2.group(1)!!
        val binaryData = try {
            Base64.getMimeDecoder().decode(URLDecoder.decode(data, "UTF-8"))
        } catch (e: Exception) {
            LogSmart.e("OTPauthUriParser", "Exception в parseGoogleMigration($url)", e)
            return emptyList()
        }
        val input = CodedInputStream.newInstance(binaryData)
        while (!input.isAtEnd) {
            val tag = input.readTag()
            if (tag == 0) break
            val fieldNumber = tag ushr 3 // Извлекаем номер поля (Tag ID)

            when (fieldNumber) {
                1 -> { // Поле otp_parameters (repeated message)
                    val length = input.readRawVarint32()
                    val oldLimit = input.pushLimit(length)
                    decodeOtpParameters(input)?.let { // Парсим вложенный объект
                        auths.add(it)
                    }
                    input.popLimit(oldLimit)
                }
                2 -> input.readInt32() // version
                3 -> input.readInt32() // batchSize
                4 -> input.readInt32() // batchIndex
                5 -> input.readInt32() // batchId
                else -> input.skipField(tag)
            }
        }
    }
    return auths
}


private fun decodeOtpParameters(input: CodedInputStream): OTPauth? {
    // Функция декодирует параметры OTP в потоке CodedInputStream.

    var secret = byteArrayOf()
    var name = ""
    var issuer = ""
    var algorithm = OtpAlgorithm.SHA1
    var digs = DigitCount.SIX
    var type = OtpType.TOTP

    while (input.bytesUntilLimit > 0 ) {
        val tag = input.readTag()
        if (tag == 0) {
            break // Конец вложенного сообщения
        }

        when (tag ushr 3) {
            1 -> secret = input.readBytes().toByteArray()
            2 -> name = input.readString()
            3 -> issuer = input.readString()
            4 -> algorithm = OtpAlgorithm.fromId(input.readEnum())
            5 -> digs = DigitCount.fromId(input.readEnum())
            6 -> type = OtpType.fromId(input.readEnum())
            else -> input.skipField(tag)
        }
    }

    val hash = when (algorithm) {
        OtpAlgorithm.SHA1 -> SHA1
        OtpAlgorithm.SHA256 -> SHA256
        OtpAlgorithm.SHA512 -> SHA512
        OtpAlgorithm.MD5 -> ""
        OtpAlgorithm.UNSPECIFIED -> SHA1
    }
    val digits = when (digs) {
        DigitCount.SIX -> 6
        DigitCount.SEVEN -> 7
        DigitCount.EIGHT -> 8
        DigitCount.UNSPECIFIED -> DEFAULT_DIGITS
    }

    val secretBase32 = Base32().encodeAsString(secret)
    return OTPauth(
            label = name,
            issuer = issuer,
            secret = Base32().encodeAsString(secret),
            hash = hash,
            digits = digits,
        ).takeIf {type == OtpType.TOTP &&
                            hash.isNotBlank() &&
                            name.isNotBlank() &&
                            isValidBase32(secretBase32)
        }
}


fun generateMigrationUrl(auths: List<OTPauth>): String {
    // Функция конвертирует список auths в url схемы otpauth-migration://offline.

    val baos = ByteArrayOutputStream()
    val codedOutput = CodedOutputStream.newInstance(baos)

    auths.forEach { auth ->
        val innerBaos = ByteArrayOutputStream()
        val innerOutput = CodedOutputStream.newInstance(innerBaos)

        val secretBytes = Base32().decode(auth.secret) // Secret (Tag 10).
        innerOutput.writeBytes(1, com.google.protobuf.ByteString.copyFrom(secretBytes))

        innerOutput.writeString(2, auth.label) // Label (Tag 18).

        innerOutput.writeString(3, auth.issuer) // Issuer (Tag 26).

        val algoId = when (auth.hash) {
            SHA1   -> 1
            SHA256 -> 2
            SHA512 -> 3
            else   -> 1 // По умолчанию SHA1.
        }
        innerOutput.writeEnum(4, algoId) // Algorithm (Tag 32).


        val digitId = when (auth.digits) {
            6 -> 1
            8 -> 2
            7 -> 3 // Нестандартный ID.
            else -> 1 // По умолчанию SIX.
        }
        innerOutput.writeEnum(5, digitId) // Digits (Tag 40).

        innerOutput.writeEnum(6, 2) // Type TOTP=2 (Tag 48).

        innerOutput.flush()
        val innerData = innerBaos.toByteArray()

        codedOutput.writeTag(1, 2) // Внешний контейнер (Tag 10).
        codedOutput.writeUInt32NoTag(innerData.size)
        codedOutput.writeRawBytes(innerData)
    }

    codedOutput.writeInt32(2, 1) // Версия (Tag 16).

    codedOutput.flush()

    val base64Data = Base64.getEncoder().encodeToString(baos.toByteArray())
    return "otpauth-migration://offline?data=${URLEncoder.encode(base64Data, "UTF-8")}"
}