package ru.mammoth70.totpgenerator

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "otpauth",
    indices = [
        Index(value = ["label"], unique = true),
        Index(value = ["secret"], unique = true)
    ]
)
data class OTPauthEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val issuer: String? = "",
    @Embedded val encryptedSecret: StringPair,
    val period: Int? = 30,
    val hash: String? = "SHA1",
    val digits: Int? = 6,
)

data class StringPair(
    @ColumnInfo(name = "secret") val encodedText: String = "",
    @ColumnInfo(name = "iv") val iv: String = ""
)