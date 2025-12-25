package ru.mammoth70.totpgenerator

import ru.mammoth70.totpgenerator.App.Companion.appContext
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit

// Переменные и функции для хранения зашифрованного pin в SharedPreferences.

var appPinCode = ""
private const val NAME_SETTINGS = "settings"
private const val NAME_PIN = "pin"
private const val NAME_IV = "iv"

fun setPin(pin: String) {
    // Зашифровать и записать pin в SharedPreferences
    val pair = encryptString(pin)
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    if (pin.isNotBlank()) {
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

fun getPin(): String {
    // Считать и расшифровать pin из SharedPreferences
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    val encryptedPin = settings.getString(NAME_PIN, "") ?: ""
    val iv = settings.getString(NAME_IV, "") ?: ""
    return if (encryptedPin.isNotBlank() && iv.isNotBlank()) {
        decryptString(StringPair(encryptedPin, iv))
    } else {
        ""
    }
}