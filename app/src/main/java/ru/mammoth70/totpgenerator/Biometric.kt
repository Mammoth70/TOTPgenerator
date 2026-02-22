package ru.mammoth70.totpgenerator

import androidx.biometric.BiometricManager
import ru.mammoth70.totpgenerator.App.Companion.appContext

var isHaveBiometric: Boolean = checkBiometricInDevice()  // Флаг наличия в смартфоне датчиков строгой биометрической идентификации.
    internal set

fun checkBiometricInDevice(): Boolean {
    // Функция проверяет наличие в смартфоне датчиков строгой биометрической идентификации.

    val biometricManager = BiometricManager.from(appContext)
    return when (biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            true
        }

        else -> {
            false
        }
    }
}