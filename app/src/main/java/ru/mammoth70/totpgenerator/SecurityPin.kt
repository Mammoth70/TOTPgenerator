package ru.mammoth70.totpgenerator

import ru.mammoth70.totpgenerator.App.Companion.appContext
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import java.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.text.isNotBlank

// Функции для проверки PIN-кода и для хранения хешированного и зашифрованного PIN-кода в SharedPreferences.

var isHaveHashPin: Boolean = checkExistsHashPin() // Флаг наличия в SharedPreferences зашифрованного хеша PIN-кода.
    internal set

private const val NAME_SETTINGS = "hashpin"
private const val NAME_PIN = "pin"
private const val NAME_IV = "iv"

private const val ITERATIONS = 10000
private const val KEY_LENGTH = 256
private const val ALGORITHM = "PBKDF2WithHmacSHA256"


fun checkExistsHashPin(): Boolean {
    // Функция проверяет наличие зашифрованного хеша PIN-кода в SharedPreferences.

    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    settings.apply {
        return (contains(NAME_PIN) && contains(NAME_IV))
    }
}


fun setHashPin(pin: CharArray) {
    // Функция зашифровывает и записывает хеш PIN-кода в SharedPreferences.

    val isDefault = pin.all { it == '\u0000' }
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    if (!isDefault) {
        val pair = encryptString(createHashFromPin(pin))
        settings.edit {
            putString(NAME_PIN, pair.encodedText)
            putString(NAME_IV, pair.iv)
        }
        isHaveHashPin = true
    } else {
        settings.edit {
            remove(NAME_PIN)
            remove(NAME_IV)
        }
        isHaveHashPin = false
    }
}


fun deleteHashPin() {
    // Функция удаляет хеш PIN-кода и его инициализационный вектор из SharedPreferences.

    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    settings.edit {
        remove(NAME_PIN)
        remove(NAME_IV)
    }
    isHaveHashPin = false
}


private fun getHashPin(): String {
    // Функция считывает из SharedPreferences и расшифровывает хеш PIN-кода.

    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    val encryptedPin = settings.getString(NAME_PIN, "") ?: ""
    val iv = settings.getString(NAME_IV, "") ?: ""
    return if (encryptedPin.isNotBlank() && iv.isNotBlank()) {
        decryptString(StringPair(encryptedPin, iv))
    } else {
        ""
    }
}


fun verifyPin(pin: CharArray): Boolean {
    // Функция сравнивает введенный PIN-код с сохраненным хешем.

    val parts = getHashPin().split(":")
    if (parts.size != 2) return false
    val salt = Base64.getDecoder().decode(parts[0]) // Декодируем соль.
    val originalHash = Base64.getDecoder().decode(parts[1])// Декодируем оригинальный хеш.
    val testHash = pbkdf2(pin, salt) // Генерируем хеш из введенного PIN-кода, используя ту же соль и итерации.
    return safeConfirm(originalHash, testHash) // Безопасное сравнение (Constant Time).
}


private fun createHashFromPin(pin: CharArray): String {
    // Функция принимает PIN-код как CharArray и возвращает строку (соль:хеш).

    val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) } // Генерируем соль.
    val hash = pbkdf2(pin, salt) // Генерируем хеш напрямую из CharArray.
    val encoder = Base64.getEncoder()
    return "${encoder.encodeToString(salt)}:${encoder.encodeToString(hash)}"
}


private fun pbkdf2(pin: CharArray, salt: ByteArray): ByteArray {
    // Функция генерирует хеш из введенного PIN-кода, используя введёную соль.

    val spec = PBEKeySpec(pin, salt, ITERATIONS, KEY_LENGTH)
    val factory = SecretKeyFactory.getInstance(ALGORITHM)
    return try {
        factory.generateSecret(spec).encoded
    } finally {
        spec.clearPassword()
    }
}


private fun safeConfirm(a: ByteArray, b: ByteArray): Boolean {
    // Функция сравнивает массивы байт способом, защищённым от атак по времени.

    if (a.size != b.size) return false
    var result = 0
    for (i in a.indices) {
        result = result or (a[i].toInt() xor b[i].toInt())
    }
    return result == 0
}