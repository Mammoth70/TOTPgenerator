package ru.mammoth70.totpgenerator

data class OTPauth(
    // Класс данных для хранения OTPauth.
    val id: Long = EMPTY_OTP,
    val label: String,
    val issuer: String = "",
    var secret: String,
    val period: Int = DEFAULT_PERIOD,
    val hash: String = SHA1,
    val digits: Int = DEFAULT_DIGITS,
)