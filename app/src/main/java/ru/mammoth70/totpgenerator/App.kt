package ru.mammoth70.totpgenerator

import android.app.Application
import android.content.Context
import java.util.concurrent.CopyOnWriteArrayList

class App : Application() {
    // Класс приложения.
    // Приложение предназначено для хранения OTPauth и вывода TOTP-токенов.

    companion object {
        lateinit var appContext: Context
        val secrets: CopyOnWriteArrayList<OTPauth> = CopyOnWriteArrayList() // Список OTPauth, считывается из БД
        val tokens: ArrayList<Token> = ArrayList() // Список токенов, вычисляемых из OTPauth

        const val SHA1 = "SHA1"
        const val SHA256 = "SHA256"
        const val SHA512 = "SHA512"
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        generateSecretKey()
        DBhelper.dbHelper.readAllSecrets()
        appPinCode = getPin()
        appPassed = getPassed()
    }

}