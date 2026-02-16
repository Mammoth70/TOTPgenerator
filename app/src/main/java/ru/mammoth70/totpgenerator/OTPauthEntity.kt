package ru.mammoth70.totpgenerator

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    // Модель базы данных.

    tableName = "otpauth",
    indices = [
        Index(value = ["label"], unique = true),
        Index(value = ["secret"], unique = true)
    ]
)


data class OTPauthEntity(
    // Класс данных, описывающий поля таблицы для хранения OTPauth.
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val issuer: String? = "",
    @Embedded val encryptedSecret: StringPair,
    val period: Int? = DEFAULT_PERIOD,
    val hash: String? = SHA1,
    val digits: Int? = DEFAULT_DIGITS,
)


data class StringPair(
    // Класс данных, описывающий пару для хранения зашифрованной строки и иницализационного вектора.
    @ColumnInfo(name = "secret") val encodedText: String = "",
    @ColumnInfo(name = "iv") val iv: String = ""
)