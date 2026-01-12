package ru.mammoth70.totpgenerator

const val FOOTER_TOKEN = -2

data class Token(
    // data класс для хранения токенов, времени и процента их жизни.
    val num: Int,
    val id: Int,
    val label: String,
    val issuer: String = "",
    val remain: Int = 0,
    val progress: Int = 100,
    var totp: String = "",
    )