package ru.mammoth70.totpgenerator

const val SHA1 = "SHA1"
const val SHA256 = "SHA256"
const val SHA512 = "SHA512"
const val EMPTY_OTP = -1L

data class OTPauth(
    // data класс для хранения OTPauth.
    val id: Long = EMPTY_OTP,
    val label: String,
    val issuer: String = "",
    var secret: String,
    val period: Int = 30,
    val hash: String = SHA1,
    val digits: Int = 6
)