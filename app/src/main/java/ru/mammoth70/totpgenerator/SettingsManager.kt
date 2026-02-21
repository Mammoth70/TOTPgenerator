package ru.mammoth70.totpgenerator

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.core.content.edit

object SettingsManager {
// Объект содержит переменные с настройками приложения.


    private const val NAME_SETTINGS = "settings"
    private const val KEY_BIO = "enablebiometric"
    private const val KEY_TIME_SHIFT = "timeshift"
    private const val KEY_NEXT_TOKEN = "enablenexttoken"
    private const val KEY_PROGRESS_CLOCK_WISE = "passed"
    private const val KEY_THEME_MODE = "thememode"

    private val prefs by lazy {
        App.appContext.getSharedPreferences(NAME_SETTINGS, Context.MODE_PRIVATE)
    }

    @Volatile
    private var cachedEnableBiometric: Boolean = prefs.getBoolean(KEY_BIO, false)
    @Volatile
    private var cachedTimeShift: Long = prefs.getLong(KEY_TIME_SHIFT, 0L)
    @Volatile
    private var cachedEnableNextToken: Boolean = prefs.getBoolean(KEY_NEXT_TOKEN, false)
    @Volatile
    private var cachedProgressClockWise: Boolean = prefs.getBoolean(KEY_PROGRESS_CLOCK_WISE, false)
    @Volatile
    private var cachedAppThemeMode =  prefs.getInt(KEY_THEME_MODE, MODE_NIGHT_FOLLOW_SYSTEM)


    var enableBiometric: Boolean  // Флаг разрешения входа по биометрии.
        get() =  cachedEnableBiometric
        set(value) {
            if (value != cachedEnableBiometric ) {
                cachedEnableBiometric = value
                prefs.edit { putBoolean(KEY_BIO, value) }
            }
        }


    var timeShift: Long  // Сдвиг времени в секундах при генерации токена для компенсации задержки времени.
        get() =  cachedTimeShift
        set(value) {
            if (value != cachedTimeShift ) {
                cachedTimeShift = value
                prefs.edit { putLong(KEY_TIME_SHIFT, value) }
            }
        }


    var enableNextToken: Boolean  // Флаг разрешения вычисления и показа следующего за текущим токена.
        get() = cachedEnableNextToken
        set(value) {
            if (value != cachedEnableNextToken ) {
                cachedEnableNextToken = value
                prefs.edit { putBoolean(KEY_NEXT_TOKEN, value) }
            }
        }


    var progressClockWise: Boolean  // Флаг показа на индикаторе прогресса, сколько прошло (false - сколько осталось).
        get() = cachedProgressClockWise
        set(value) {
            if (value != cachedProgressClockWise ) {
                cachedProgressClockWise = value
                prefs.edit { putBoolean(KEY_PROGRESS_CLOCK_WISE, value) }
            }
        }


    var appThemeMode: Int    // Режим темы приложения.
        get() = cachedAppThemeMode
        set(value) {
            if (value != cachedAppThemeMode ) {
                cachedAppThemeMode = value
                installThemeMode(value)
                prefs.edit { putInt(KEY_THEME_MODE, value) }
            }
        }


    fun installThemeMode() {
        // Функция устанавливает режим темы в приложении. (для внешнего вызова)
        installThemeMode(appThemeMode)
    }
    private fun installThemeMode(mode: Int) {
        // Функция устанавливает режим темы в приложении.

        when (mode) {
            MODE_NIGHT_NO -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
            MODE_NIGHT_YES -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

}