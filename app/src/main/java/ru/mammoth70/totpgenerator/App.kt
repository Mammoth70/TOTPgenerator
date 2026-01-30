package ru.mammoth70.totpgenerator

import android.app.Application
import android.content.Context
import java.util.concurrent.CopyOnWriteArrayList

class App : Application() {
    // Класс приложения.
    // Приложение предназначено для хранения OTPauth и вывода TOTP-токенов.

    companion object {
        lateinit var appContext: Context
            private set
        val appSecrets: CopyOnWriteArrayList<OTPauth> = CopyOnWriteArrayList() // Список OTPauth, считывается из БД
        val appTokens: ArrayList<Token> = ArrayList() // Список токенов, вычисляемых из OTPauth
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