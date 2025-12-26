package ru.mammoth70.totpgenerator

const val SHA1 = "SHA1"
const val SHA256 = "SHA256"
const val SHA512 = "SHA512"

data class OTPauth(
    // data класс для хранения OTPauth.
    val num: Int= -1,
    val id: Int= -1,
    val label: String,
    val issuer: String = "",
    var secret: String,
    val period: Int = 30,
    val hash: String = SHA1,
    val digits: Int = 6
)