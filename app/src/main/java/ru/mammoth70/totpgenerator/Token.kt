package ru.mammoth70.totpgenerator

data class Token(
    // Класс данных для хранения токенов, оставшегося времени жизни и оставшегося процента жизни.
    val id: Long,
    val label: String,
    val issuer: String = "",
    val remain: Int = 0,
    val progress: Int = 100,
    var totp: String = "",
    var totpNext: String = "",
    )