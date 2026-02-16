package ru.mammoth70.totpgenerator

const val SHA1 = "SHA1"
const val SHA256 = "SHA256"
const val SHA512 = "SHA512"
const val DEFAULT_PERIOD = 30
const val DEFAULT_DIGITS = 6
const val EMPTY_OTP = -1L


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