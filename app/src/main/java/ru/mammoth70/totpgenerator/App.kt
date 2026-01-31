package ru.mammoth70.totpgenerator

import android.app.Application
import android.content.Context

class App : Application() {
    // Класс приложения.
    // Приложение предназначено для хранения OTPauth и вывода TOTP-токенов.

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        generateSecretKey()
        DBhelper.dbHelper.readAllSecrets()
        checkExistsHashPin()
        getProgressMode()
        getThemeMode()
        checkBiometricInDevice()
        getBiometricLogin()
    }

}