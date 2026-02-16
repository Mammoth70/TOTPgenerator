package ru.mammoth70.totpgenerator

import android.util.Log

// Статические функции.


fun isValidBase32(secret: String): Boolean {
    // Функция проверяет строку Base32 на валидность.

    // Проверка на пустоту.
    if (secret.isBlank()) return false

    // Проверка алфавита (A-Z и 2-7).
    // Допускаем Padding (=), но обычно в ссылках его нет.
    val base32Regex = Regex("^[A-Z2-7]+=*$")
    if (!base32Regex.matches(secret.uppercase())) return false

    // Проверка кратности согласно RFC 4648.
    // Полный блок Base32 должен быть кратен 8 символам.
    // Если padding отсутствует (что часто в QR), длина должна быть 2, 4, 5, 7 символов в остатке от деления на 8.
    // Длины с остатком 1, 3, 6 — некорректны.
    if (secret.contains("=") && secret.length % 8 != 0) return false
    val lengthMod = secret.filter { it != '=' }.length % 8
    val invalidMods = listOf(1, 3, 6)
    return lengthMod !in invalidMods
}


object LogSmart {
    // Функции выводят в лог ошибки и отладочные сообщения, только если приложение собрано для отладки.

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable)
        }
    }

    @Suppress("unused")
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
}