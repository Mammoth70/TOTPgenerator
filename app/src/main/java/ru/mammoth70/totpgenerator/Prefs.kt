package ru.mammoth70.totpgenerator

import android.content.Context.MODE_PRIVATE
import ru.mammoth70.totpgenerator.App.Companion.appContext
import androidx.core.content.edit

// Переменные и функции для хранения установок в SharedPreferences.

var appPassed = true
var appEnableBiometric = false

private const val NAME_SETTINGS = "settings"
private const val NAME_PASSED = "passed"
private const val NAME_BIO = "enablebiometric"

fun setPassed() {
    // Записать способ показа Progress в SharedPreferences
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    settings.edit {
        putBoolean(NAME_PASSED, appPassed)
    }
}

fun getPassed() {
    // Считать способ показа Progress из SharedPreferences
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    appPassed =  settings.getBoolean(NAME_PASSED, true)
}

fun setEnableBiometric() {
    // Записать разрешение аутентификации по биометрии в SharedPreferences
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    settings.edit {
        putBoolean(NAME_BIO, appEnableBiometric && isHaveBiometric)
    }
}

fun getEnableBiometric() {
    // Считать разрешение аутентификации по биометрии из SharedPreferences
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    appEnableBiometric =  settings.getBoolean(NAME_BIO, false) && isHaveBiometric
}
