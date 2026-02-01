package ru.mammoth70.totpgenerator

import android.app.Application
import android.content.Context

class App : Application() {
    // Класс приложения.
    // Приложение предназначено для хранения секретов-OTPauth и вывода TOTP-токенов.

    companion object {
        lateinit var appContext: Context    // В этой переменной хранится контекст приложения.
            private set
    }

    override fun onCreate() {
        // Выполняется один раз, при старте приложения.
        super.onCreate()
        appContext = applicationContext
        generateSecretKey()
        DBhelper.dbHelper.readAllSecrets()
        checkExistsHashPin()
        getProgressMode()
        getNextToken()
        getThemeMode()
        checkBiometricInDevice()
        getBiometricLogin()
    }

}