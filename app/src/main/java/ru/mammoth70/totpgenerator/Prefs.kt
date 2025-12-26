package ru.mammoth70.totpgenerator

import android.content.Context.MODE_PRIVATE
import ru.mammoth70.totpgenerator.App.Companion.appContext
import androidx.core.content.edit

// Переменные и функции для хранения установок в SharedPreferences.

var appPassed = true

private const val NAME_SETTINGS = "settings"
private const val NAME_PASSED = "passed"

fun setPassed(passed: Boolean) {
    // Записать способ показа Progress в SharedPreferences
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    settings.edit {
        putBoolean(NAME_PASSED, passed)
    }
}

fun getPassed(): Boolean {
    // Считать способ показа Progress из SharedPreferences
    val settings = appContext.getSharedPreferences(NAME_SETTINGS, MODE_PRIVATE)
    return settings.getBoolean(NAME_PASSED, true)
}