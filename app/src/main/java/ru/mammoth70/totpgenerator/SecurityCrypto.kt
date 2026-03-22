package ru.mammoth70.totpgenerator

import android.security.keystore.KeyProperties
import android.security.keystore.KeyGenParameterSpec
import java.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// Функции для работы с защищенным хранилищем AndroidKeyStore.

private const val PROVIDER = "AndroidKeyStore"
private const val KEY_ALIAS = "key_totp_generator"
private const val TRANSFORMATION = "AES/GCM/NoPadding"


@Suppress("unused")
fun isSecretKey(): Boolean {
    // Функция проверяет наличие в AndroidKeyStore ключа для шифрования TOTP секретов.
    try {
        val keyStore = KeyStore.getInstance(PROVIDER)
        keyStore.load(null)
        return keyStore.containsAlias(KEY_ALIAS)
    } catch (e: Exception) {
        LogSmart.e("SecurityCrypto", "Exception в isSecretKey", e)
    }
    return false
}


fun generateSecretKey() {
    // Функция создаёт в AndroidKeyStore ключ для шифрования TOTP секретов, если его нет.

    try {
        val keyStore = KeyStore.getInstance(PROVIDER)
        keyStore.load(null)
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, PROVIDER
        )
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    } catch (e: Exception) {
        LogSmart.e("SecurityCrypto", "Exception в generateSecretKey", e)
    }
}


@Suppress("unused")
fun deleteSecretKey() {
    // Функция удаляет из AndroidKeyStore ключ для шифрования TOTP секретов, если он есть.
    try {
        val keyStore = KeyStore.getInstance(PROVIDER)
        keyStore.load(null)
        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
    } catch (e: Exception) {
        LogSmart.e("SecurityCrypto", "Exception в deleteSecretKey", e)
    }
}


private fun getSecretKey(): SecretKey? {
    // Функция получает из AndroidKeyStore ключ для шифрования TOTP секретов, если он есть.

    try {
        val keyStore = KeyStore.getInstance(PROVIDER)
        keyStore.load(null)
        return if (keyStore.containsAlias(KEY_ALIAS)) {
            (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        } else {
            null
        }
    } catch (e: Exception) {
        LogSmart.e("SecurityCrypto", "Exception в getSecretKey", e)
    }
    return null
}


fun encryptString(startedText: String): StringPair {
    // Функция шифрует заданную строку с помощью AES GSM на ключе для шифрования TOTP секретов.
    // Функция возвращает пару из зашифрованной строки и инициализационного вектора.

    try {
        val plaintext: ByteArray = startedText.toByteArray()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val encodedBytes = cipher.doFinal(plaintext)

        // Конвертация зашифрованных данных в base64.
        val encodedText = Base64.getEncoder().encodeToString(encodedBytes)
        val iv = Base64.getEncoder().encodeToString(cipher.iv)

        // Возврат зашифрованных данных с вектором инициализации.
        return StringPair(encodedText, iv)

    } catch (e: Exception) {
        LogSmart.e("SecurityCrypto", "Exception в encryptString", e)
        return StringPair()
    }
}


fun decryptString(encryptedPair: StringPair): String {
    // Функция возвращает расшифрованную заданную строку с помощью AES GSM ключом для шифрования TOTP секретов.
    // В функцию передаётся пара из зашифрованной строки и инициализационного вектора.

    try {
        // Конвертация зашифрованных данных из base64.
        val iv = Base64.getDecoder().decode(encryptedPair.iv)
        val encodedBytes = Base64.getDecoder().decode(encryptedPair.encodedText)

        // Расшифрование зашифрованных данных.
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val ivParams = GCMParameterSpec(128, iv)

        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), ivParams)
        val decodedBytes = cipher.doFinal(encodedBytes)

        // Возврат расшифрованных данных.
        return String(decodedBytes!!)

    } catch (e: Exception) {
        LogSmart.e("SecurityCrypto", "Exception в decryptString", e)
        return ""
    }

}