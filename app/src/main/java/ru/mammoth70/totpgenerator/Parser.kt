package ru.mammoth70.totpgenerator

import java.util.regex.Pattern
import java.net.URLDecoder
import android.util.Base64
import com.google.protobuf.CodedInputStream
import org.apache.commons.codec.binary.Base32

// Парсер разбора строки схем otpauth://totp и otpauth-migration://offline

private const val REGEXP_HEAD1 = "^otpauth://totp/(\\S+?)\\?"
private const val REGEXP_HEAD2 = "^otpauth-migration://offline\\?data=(\\S+?)$"
private const val REGEXP_SECRET = "[?&]secret=([2-7A-Z]+?)(&|$)"
private const val REGEXP_ISSUER = "[?&]issuer=(\\S+?)(&|$)"
private const val REGEXP_PERIOD = "[?&]period=(\\d+?)(&|$)"
private const val REGEXP_ALGORITHM = "[?&]algorithm=(SHA1|SHA256|SHA512)(&|$)"
private const val REGEXP_DIGITS = "[?&]digits=([6-8])(&|$)"

private enum class OtpAlgorithm(val id: Int) {
    UNSPECIFIED(0), SHA1(1), SHA256(2), SHA512(3), MD5(4);
    companion object {
        fun fromId(id: Int) = entries.find { it.id == id } ?: SHA1
    }
}

private enum class DigitCount(val id: Int) {
    UNSPECIFIED(0), SIX(1), EIGHT(2);
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
    // Функция разбирает строку url
    val auths = mutableListOf<OTPauth>()
    if (url.isNullOrBlank()) {
        return auths
    }

    val pattern1 = Pattern.compile(REGEXP_HEAD1)
    val matcher1 = pattern1.matcher(url)
    val pattern2 = Pattern.compile(REGEXP_HEAD2)
    val matcher2 = pattern2.matcher(url)
    if ((matcher1.find())) {
        val result = parseOTPauth(url)
        if (result != null) {
            auths.add(result)
        }
    } else if ((matcher2.find())) {
        val results = parseGoogleMigration(url)
        results.forEach { auths.add(it) }
    }
    return auths
}

fun parseOTPauth(url: String): OTPauth?  {
    // Функция разбирает строку url otpauth://totp и в случае удачи возвращат OTPauth, в противном случае - null.

    val pattern1 = Pattern.compile(REGEXP_HEAD1)
    val matcher1 = pattern1.matcher(url)
    if ((!matcher1.find())) {
        return null
    }
    if (matcher1.group(1).isNullOrBlank()) {
        return null
    }
    var label = matcher1.group(1)!!

    val pattern2 = Pattern.compile(REGEXP_SECRET)
    val matcher2 = pattern2.matcher(url)
    if ((!matcher2.find())) {
        return null
    }
    if (matcher2.group(1).isNullOrBlank()) {
        return null
    }
    val secret = matcher2.group(1)!!

    val pattern3 = Pattern.compile(REGEXP_ISSUER)
    val matcher3 = pattern3.matcher(url)
    var issuer = ""
    if ((matcher3.find())) {
        if (!matcher3.group(1).isNullOrBlank()) {
            issuer = matcher3.group(1)!!
        }
    }
    if (label.startsWith("$issuer:")) {
        // Google значение issuer хранит в поле label через двоеточие.
        label = label.replace("$issuer:","")
    }

    val pattern4 = Pattern.compile(REGEXP_PERIOD)
    val matcher4 = pattern4.matcher(url)
    var period = 30
    if ((matcher4.find())) {
        if (!matcher4.group(1).isNullOrBlank()) {
            period = matcher4.group(1)!!.toInt()
        }
    }

    val pattern5 = Pattern.compile(REGEXP_ALGORITHM)
    val matcher5 = pattern5.matcher(url)
    var hash = SHA1
    if ((matcher5.find())) {
        if (!matcher5.group(1).isNullOrBlank()) {
            hash = matcher5.group(1)!!
        }
    }

    val pattern6 = Pattern.compile(REGEXP_DIGITS)
    val matcher6 = pattern6.matcher(url)
    var digits = 6
    if ((matcher6.find())) {
        if (!matcher6.group(1).isNullOrBlank()) {
            digits = matcher6.group(1)!!.toInt()
        }
    }

    val auth = OTPauth(
        label = label,
        secret = secret,
        issuer = issuer,
        period = period,
        hash = hash,
        digits = digits,
    )

    return auth
}

fun parseGoogleMigration(url: String): List<OTPauth> {
    // Функция разбирает строку url otpauth-migration://offline,
    // и в случае удачи возвращат список OTPauth, в противном случае - список возвращается пустым.
    val results = mutableListOf<OTPauth>()
    val pattern2 = Pattern.compile(REGEXP_HEAD2)
    val matcher2 = pattern2.matcher(url)
    if ((matcher2.find()) && (!matcher2.group(1).isNullOrBlank())) {
        val data = matcher2.group(1)!!
        val binaryData = Base64.decode(URLDecoder.decode(data, "UTF-8"),Base64.DEFAULT)
        val input = CodedInputStream.newInstance(binaryData)
        while (!input.isAtEnd) {
            val tag = input.readTag()
            val fieldNumber = tag ushr 3 // Извлекаем номер поля (Tag ID)

            when (fieldNumber) {
                1 -> { // Поле otp_parameters (repeated message)
                    val length = input.readRawVarint32()
                    val oldLimit = input.pushLimit(length)
                    val res = parseOtpParameters(input) // Парсим вложенный объект
                    if (res != null) {
                        results.add(res)
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
    return results
}

private fun parseOtpParameters(input: CodedInputStream): OTPauth? {
    // Функция разбирает параметры OTP в потоке CodedInputStream
    var secret = byteArrayOf()
    var name = ""
    var issuer = ""
    var algorithm = OtpAlgorithm.SHA1
    var digs = DigitCount.SIX
    var type = OtpType.TOTP

    while (!input.isAtEnd) {
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
        OtpAlgorithm.SHA1 -> "SHA1"
        OtpAlgorithm.SHA256 -> "SHA256"
        OtpAlgorithm.SHA512 -> "SHA512"
        OtpAlgorithm.MD5 -> ""
        OtpAlgorithm.UNSPECIFIED -> "SHA1"
    }
    val digits = when (digs) {
        DigitCount.SIX -> 6
        DigitCount.EIGHT -> 8
        DigitCount.UNSPECIFIED -> 6
    }

    return if ((type == OtpType.TOTP) && (hash.isNotBlank()) && (name.isNotBlank())) {
        OTPauth(
            label = name,
            issuer = issuer,
            secret = Base32().encodeAsString(secret),
            hash = hash,
            digits = digits
        )
    } else {
        null
    }
}