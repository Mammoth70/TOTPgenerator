package ru.mammoth70.totpgenerator

import android.content.Context.MODE_PRIVATE
import androidx.appcompat.app.AppCompatDelegate
import ru.mammoth70.totpgenerator.App.Companion.appContext
import androidx.core.content.edit

// Переменные и функции для хранения установок в SharedPreferences.

var appThemeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    internal set
var progressClockWise: Boolean = true
    internal set
var enableBiometric: Boolean = false
    internal set

private const val NAME_SETTINGS = "settings"
private const val NAME_PASSED = "passed"
private const val NAME_THEME_MODE = "thememode"
private const val NAME_BIO = "enablebiometric"

fun setProgressMode(passed: Boolean) {
    // Записать способ показа Progress в SharedPreferences
    progressClockWise = passed
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    settings.edit {
        putBoolean(NAME_PASSED, progressClockWise)
    }
}

fun getProgressMode() {
    // Считать способ показа Progress из SharedPreferences
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    progressClockWise =  settings.getBoolean(NAME_PASSED, true)
}

private fun installThemeMode() {
    when (appThemeMode) {
        AppCompatDelegate.MODE_NIGHT_NO -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}

fun setThemeMode(mode: Int) {
    // Записать режим темы в SharedPreferences
    appThemeMode = mode
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    settings.edit {
        putInt(NAME_THEME_MODE, appThemeMode)
    }
    installThemeMode()
}

fun getThemeMode() {
    // Считать режим темы из SharedPreferences
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    appThemeMode =  settings.getInt(NAME_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    installThemeMode()
}

fun setBiometricLogin(biomeric: Boolean) {
    // Записать разрешение аутентификации по биометрии в SharedPreferences
    enableBiometric = biomeric
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    settings.edit {
        putBoolean(NAME_BIO, enableBiometric && isHaveBiometric)
    }
}

fun getBiometricLogin() {
    // Считать разрешение аутентификации по биометрии из SharedPreferences
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    enableBiometric =  settings.getBoolean(NAME_BIO, false) && isHaveBiometric
}
