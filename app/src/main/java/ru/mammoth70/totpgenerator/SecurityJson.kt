package ru.mammoth70.totpgenerator

import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

// Функция экспорта импорта JSON в файл с шифрованием паролем и в открытом виде.

const val ERROR_WRONG_PASSWORD = "ERROR_WRONG_PASSWORD"
const val ERROR_INVALID_FILE_TYPE = "ERROR_INVALID_FILE_TYPE"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val ALGORITHM = "PBKDF2WithHmacSHA256"
private const val ALGORITHM1 = "AES"
private const val ITERATIONS = 600000
private const val KEY_LENGTH = 256
private const val T_LENGTH = 128
private const val FILE_HEADER_PLAIN = "TOTPgen0"
private const val FILE_HEADER_ENCRYPTED = "TOTPgen1"
private val HEADER_BYTES_PLAIN = FILE_HEADER_PLAIN.toByteArray(Charsets.US_ASCII)
private val HEADER_BYTES_ENCRYPTED = FILE_HEADER_ENCRYPTED.toByteArray(Charsets.US_ASCII)


fun encryptAndWrite(jsonText: String, password: String, outputStream: OutputStream) {
    // Функция необязательного шифрования JSON паролем и выгрузки в файл.

    try {
        outputStream.use { os ->
            if (password.isEmpty()) {
                // JSON сохраним в файл в открытом виде.
                os.write(HEADER_BYTES_PLAIN)
                os.write(jsonText.toByteArray(Charsets.UTF_8))

            } else {
                // JSON сохраним в файл в зашифрованом виде.
                val secureRandom = SecureRandom()
                val salt = ByteArray(16).apply { secureRandom.nextBytes(this) }
                val iv = ByteArray(12).apply { secureRandom.nextBytes(this) }

                val factory = SecretKeyFactory.getInstance(ALGORITHM)
                val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
                val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, ALGORITHM1)

                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(T_LENGTH, iv))
                val encryptedBytes = cipher.doFinal(jsonText.toByteArray(Charsets.UTF_8))

                os.write(HEADER_BYTES_ENCRYPTED)
                os.write(salt)
                os.write(iv)
                os.write(encryptedBytes)
            }
            os.flush()
        }


    } catch (e: Exception) {
        LogSmart.e("SecurityJson", "Exception в encryptAndWrite", e)
        throw e
    }
}


fun readAndDecrypt(inputStream: InputStream, password: String): String {
    // Функция загрузки открытого или зашифрованного JSON из файла и расшифровки по необходимости.

    try {
        inputStream.use { stream ->
            val allBytes = stream.readBytes()
            if (allBytes.size < 8) return ERROR_INVALID_FILE_TYPE
            val buffer = ByteBuffer.wrap(allBytes)

            val fileHeader = ByteArray(HEADER_BYTES_ENCRYPTED.size)
            buffer.get(fileHeader)

            return when {
                // Это незашифрованный файл.
                fileHeader.contentEquals(HEADER_BYTES_PLAIN) -> {
                    val plainData = ByteArray(buffer.remaining())
                    buffer.get(plainData)
                    String(plainData, Charsets.UTF_8)
                }

                // Это зашифрованный файл.
                fileHeader.contentEquals(HEADER_BYTES_ENCRYPTED) -> {

                    if (password.isEmpty()) return ERROR_WRONG_PASSWORD

                    val salt = ByteArray(16).apply { buffer.get(this) }
                    val iv = ByteArray(12).apply { buffer.get(this) }
                    val encryptedData = ByteArray(buffer.remaining()).apply { buffer.get(this) }

                    val factory = SecretKeyFactory.getInstance(ALGORITHM)
                    val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
                    val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, ALGORITHM1)

                    val cipher = Cipher.getInstance(TRANSFORMATION)
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(T_LENGTH, iv))

                    String(cipher.doFinal(encryptedData), Charsets.UTF_8)
                }

                // Это неизвестный формат файла.
                else -> ERROR_INVALID_FILE_TYPE
            }
        }

    } catch (_: AEADBadTagException) {
        return ERROR_WRONG_PASSWORD
    } catch (e: Exception) {
        LogSmart.e("SecurityJson", "Exception в readAndDecrypt", e)
        return ""
    }
}