package ru.mammoth70.totpgenerator

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        ProcessLifecycleOwner.get().lifecycleScope.launch(Dispatchers.IO) {
            OTPauthDataRepo.readAllSecrets()
        }
        SettingsManager.installThemeMode()
    }

}