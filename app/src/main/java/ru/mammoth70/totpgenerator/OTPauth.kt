package ru.mammoth70.totpgenerator

import ru.mammoth70.totpgenerator.App.Companion.SHA1

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