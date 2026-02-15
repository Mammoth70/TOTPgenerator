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
    // data класс, описывающий поля таблицы для хранения OTPauth.
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val issuer: String? = "",
    @Embedded val encryptedSecret: StringPair,
    val period: Int? = 30,
    val hash: String? = "SHA1",
    val digits: Int? = 6,
)

data class StringPair(
    // data класс, описывающий пару для хранения зашифрованной строки и иницализационного вектора.
    @ColumnInfo(name = "secret") val encodedText: String = "",
    @ColumnInfo(name = "iv") val iv: String = ""
)