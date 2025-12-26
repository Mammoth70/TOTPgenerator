@file:Suppress("unused")
package ru.mammoth70.totpgenerator

import android.security.keystore.KeyProperties
import android.security.keystore.KeyGenParameterSpec
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import java.time.LocalTime
import java.time.temporal.ChronoField
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// Функции для работы с защищенным хранилищем AndroidKeyStore.

data class StringPair(
    // data класс для хранения пары из зашифрованной строки и инициализационного вектора
    val encodedText: String = "",
    val iv: String = "",
)

private const val PROVIDER = "AndroidKeyStore"
private const val KEY_ALIAS = "key_totp_generator"
private const val SHA1PRNG = "SHA1PRNG"
private const val TRANSFORMATION = "AES/GCM/NoPadding"

fun isSecretKey(): Boolean {
    // Функция проверяет наличие в AndroidKeyStore ключа для шифрования TOTP секретов.
    val keyStore = KeyStore.getInstance(PROVIDER)
    keyStore.load(null)
    return keyStore.containsAlias(KEY_ALIAS)
}

fun generateSecretKey() {
    // Функция создаёт в AndroidKeyStore ключ для шифрования TOTP секретов, если его нет.
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
        .setUnlockedDeviceRequired(true)
        .build()

    val random = SecureRandom.getInstance(SHA1PRNG)
    val seed =
        LocalTime.now().getLong(ChronoField.MILLI_OF_DAY).toString() +
                LocalTime.now().getLong(ChronoField.MICRO_OF_SECOND).toString()
    random.setSeed(seed.toByteArray())
    keyGenerator.init(keyGenParameterSpec, random)
    keyGenerator.generateKey()
}

fun deleteSecretKey() {
    // Функция удаляет из AndroidKeyStore ключ для шифрования TOTP секретов, если он есть.
    val keyStore = KeyStore.getInstance(PROVIDER)
    keyStore.load(null)
    if (keyStore.containsAlias(KEY_ALIAS)) {
        keyStore.deleteEntry(KEY_ALIAS)
    }
}

private fun getSecretKey(): SecretKey? {
    // Функция получает из AndroidKeyStore ключ для шифрования TOTP секретов, если он есть.
    val keyStore = KeyStore.getInstance(PROVIDER)
    keyStore.load(null)
    return if (keyStore.containsAlias(KEY_ALIAS)) {
        (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    } else {
        null
    }
}

fun encryptString(startedText: String): StringPair {
    // Функция шифрует заданную строку с помощью AES GSM на ключе для шифрования TOTP секретов.
    try {
        val plaintext: ByteArray = startedText.toByteArray()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val encodedBytes = cipher.doFinal(plaintext)

        // Конвертация зашифрованных данных в base64.
        val encodedText = Base64.encodeToString(encodedBytes, Base64.DEFAULT).trimEnd { it == '\n' }
        val iv = Base64.encodeToString(cipher.iv, Base64.DEFAULT).trimEnd { it == '\n' }

        // Возврат зашифрованных данных с вектором инициализации.
        return StringPair(encodedText, iv)

    } catch (_: java.lang.Exception) {
        return StringPair()
    }
}

fun decryptString(encryptedPair: StringPair): String {
    // Функция расшифровывает заданную строку с помощью AES GSM ключом для шифрования TOTP секретов.
    // В фунцию передаётся пара из зашифрованной строки и инициализационного вектора
    try {
        // Конвертация зашифрованных данных из base64.
        val iv = Base64.decode(encryptedPair.iv, Base64.DEFAULT)
        val encodedBytes = Base64.decode(encryptedPair.encodedText, Base64.DEFAULT)

        // Расшифрование зашифрованных данных.
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val ivParams = GCMParameterSpec(128, iv)

        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), ivParams)
        val decodedBytes = cipher.doFinal(encodedBytes)

        // Возврат расшифрованных данных.
        return String(decodedBytes!!)

    } catch (_: java.lang.Exception) {
        return ""
    }
}