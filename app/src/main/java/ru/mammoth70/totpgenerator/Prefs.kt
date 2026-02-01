package ru.mammoth70.totpgenerator

import android.content.Context.MODE_PRIVATE
import androidx.appcompat.app.AppCompatDelegate
import ru.mammoth70.totpgenerator.App.Companion.appContext
import androidx.core.content.edit

// Переменные и функции для хранения установок в SharedPreferences.

var appThemeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    internal set    // Режим темы.
var progressClockWise: Boolean = true
    internal set    // Флаг показа на индикаторе прогресса, сколько прошло (false - сколько осталось).
var enableBiometric: Boolean = false
    internal set    // Флаг разрешения входа по биометрии.

var enableNextToken: Boolean = false
    internal set    // Флаг разрешения вычисления и показа следующего за текущим токена.

private const val NAME_SETTINGS = "settings"
private const val NAME_PASSED = "passed"
private const val NAME_THEME_MODE = "thememode"
private const val NAME_BIO = "enablebiometric"
private const val NAME_NEXT_TOKEN = "enablenexttoken"

fun setProgressMode(passed: Boolean) {
    // Функция устанавливает флаг показа прогресса и записывает его в SharedPreferences.
    progressClockWise = passed
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    settings.edit {
        putBoolean(NAME_PASSED, progressClockWise)
    }
}

fun getProgressMode() {
    // Функция считывает флаг показа прогресса из SharedPreferences и устанавливает его.
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    progressClockWise =  settings.getBoolean(NAME_PASSED, true)
}

fun setNextToken(passed: Boolean) {
    // Функция устанавливает флаг показа следующего токена и записывает его в SharedPreferences.
    enableNextToken = passed
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    settings.edit {
        putBoolean(NAME_NEXT_TOKEN, enableNextToken)
    }
}

fun getNextToken() {
    // Функция считывает флаг показа следующего токена из SharedPreferences и устанавливает его.
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    enableNextToken =  settings.getBoolean(NAME_NEXT_TOKEN, false)
}

private fun installThemeMode() {
    // Функция устанавливает режим темы.
    when (appThemeMode) {
        AppCompatDelegate.MODE_NIGHT_NO -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}

fun setThemeMode(mode: Int) {
    // Функция устанавливает режим темы и записывет его в SharedPreferences.
    appThemeMode = mode
    installThemeMode()
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    settings.edit {
        putInt(NAME_THEME_MODE, appThemeMode)
    }
}

fun getThemeMode() {
    // Функция считывает режим темы из SharedPreferences и устанавливает его.
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    appThemeMode =  settings.getInt(NAME_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    installThemeMode()
}

fun setBiometricLogin(biomeric: Boolean) {
    // Функция устанавливает флаг разрешения аутентификации по биометрии и записывает его в SharedPreferences.
    enableBiometric = biomeric
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    settings.edit {
        putBoolean(NAME_BIO, enableBiometric && isHaveBiometric)
    }
}

fun getBiometricLogin() {
    // Функция считывает флаг разрешения аутентификации по биометрии из SharedPreferences и устанавливает его.
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    enableBiometric =  settings.getBoolean(NAME_BIO, false) && isHaveBiometric
}