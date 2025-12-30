package ru.mammoth70.totpgenerator

import ru.mammoth70.totpgenerator.App.Companion.appContext
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit

// Переменные и функции для хранения зашифрованного pin в SharedPreferences.

var appPinCode = ""
private const val NAME_SETTINGS = "settings"
private const val NAME_PIN = "pin"
private const val NAME_IV = "iv"

fun setPin() {
    // Зашифровать и записать pin в SharedPreferences
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    if (appPinCode.isNotBlank()) {
        val pair = encryptString(appPinCode)
        settings.edit {
            putString(NAME_PIN, pair.encodedText)
            putString(NAME_IV, pair.iv)
        }
    } else {
        settings.edit {
            remove(NAME_PIN)
            remove(NAME_IV)
        }
    }
}

fun getPin() {
    // Считать и расшифровать pin из SharedPreferences
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    val encryptedPin = settings.getString(NAME_PIN, "") ?: ""
    val iv = settings.getString(NAME_IV, "") ?: ""
    appPinCode = if (encryptedPin.isNotBlank() && iv.isNotBlank()) {
        decryptString(StringPair(encryptedPin, iv))
    } else {
        ""
    }
}