@file:Suppress("unused")
package ru.mammoth70.totpgenerator

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ru.mammoth70.totpgenerator.App.Companion.appContext
import ru.mammoth70.totpgenerator.App.Companion.appSecrets
import ru.mammoth70.totpgenerator.App.Companion.appTokens
import java.sql.SQLException

class DBhelper(context: Context?) : SQLiteOpenHelper(context, "totpDB",
        null, DB_VERSION) {
    // Класс обслуживает базу данных со списком OTPauth.
    // Поле secret хранится зашифрованным, в поле iv лежит инициализационный вектор

    companion object {
        const val OK = 0
        const val ERR_SQL_EXCEPT = 100
        const val ERR_CRYPTO = 50
        const val ERR_RES_COUNT = 2
        const val ERR_LIST_COUNT = 1

        private const val DB_VERSION = 1 // версия БД
        val dbHelper = DBhelper(appContext)
        private const val CREATE_TABLE_SECRETS = "CREATE TABLE IF NOT EXISTS otpauth " +
                "(id integer PRIMARY KEY AUTOINCREMENT, " +
                "label text UNIQUE NOT NULL, issuer text DEFAULT '', " +
                "secret text UNIQUE NOT NULL, iv NOT NULL, " +
                "step integer DEFAULT 30, hash text DEFAULT 'SHA1', digits integer DEFAULT 6);"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Функция создаёт таблицу OTPauth.
        db.execSQL(CREATE_TABLE_SECRETS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Функция делает апгрейд БД.
        // Пока, это заглушка.
    }

    fun readAllSecrets() {
        // Функция считывает все OTPauth в список secrets и предзаполняет список tokens.
        appSecrets.clear()
        appTokens.clear()
        readableDatabase.use { db ->
            db.rawQuery("SELECT * FROM otpauth ORDER BY id;", null).use { cursor ->
                var num = 0
                while (cursor.moveToNext()) {
                    val encryptedPair = StringPair(
                        encodedText = cursor.getString(
                        cursor.getColumnIndexOrThrow("secret")),
                        iv = cursor.getString(
                        cursor.getColumnIndexOrThrow("iv")),
                        )
                    val secret = OTPauth(
                        num = num,
                        id = cursor.getInt(
                            cursor.getColumnIndexOrThrow("id")),
                        label = cursor.getString(
                            cursor.getColumnIndexOrThrow("label")),
                        issuer = cursor.getString(
                            cursor.getColumnIndexOrThrow("issuer")),
                        secret = decryptString(encryptedPair),
                        period = cursor.getInt(
                            cursor.getColumnIndexOrThrow("step")),
                        hash = cursor.getString(
                            cursor.getColumnIndexOrThrow("hash")),
                        digits = cursor.getInt(
                            cursor.getColumnIndexOrThrow("digits")),
                    )
                    val token = Token(
                        num = num,
                        id = cursor.getInt(
                            cursor.getColumnIndexOrThrow("id")),
                        label = cursor.getString(
                            cursor.getColumnIndexOrThrow("label")),
                        issuer = cursor.getString(
                            cursor.getColumnIndexOrThrow("issuer")),
                    )
                    appSecrets.add(secret)
                    appTokens.add(token)
                    num ++
                }
            }
        }
    }

    fun addSecret(otpauth: OTPauth): Int  {
        // Функция добавляет запись OTPauth в БД.
        // Возвращает 0, если успешно и <> 0, если нет.
        if (otpauth.label in appSecrets.map(OTPauth::label)) {
            return ERR_LIST_COUNT
        }
        val pair = encryptString(otpauth.secret)
        if (pair.encodedText.isEmpty() || pair.iv.isEmpty()) {
            return ERR_CRYPTO
        }
        writableDatabase.use { db ->
            try {
                val values = ContentValues()
                values.put("label", otpauth.label)
                values.put("issuer", otpauth.issuer)
                values.put("secret", pair.encodedText)
                values.put("iv", pair.iv)
                values.put("step", otpauth.period)
                values.put("hash", otpauth.hash)
                values.put("digits", otpauth.digits)
                val result = db.insert("otpauth", null, values)
                if (result == -1L) {
                    return ERR_RES_COUNT
                }
            } catch (_: SQLException) {
                return ERR_SQL_EXCEPT
            }
        }
        return OK
    }

    fun editSecret(otpauth: OTPauth): Int {
        // Функция обновляет запись OTPauth в БД.
        // Поиск записи идёт по полю id.
        // Возвращает 0, если успешно и <> 0, если нет.
        val pair = encryptString(otpauth.secret)
        if (pair.encodedText.isEmpty() || pair.iv.isEmpty()) {
            return ERR_CRYPTO
        }
        writableDatabase.use { db ->
            try {
                val values = ContentValues()
                values.put("label", otpauth.label)
                values.put("issuer", otpauth.issuer)
                values.put("secret", pair.encodedText)
                values.put("iv", pair.iv)
                values.put("step", otpauth.period)
                values.put("hash", otpauth.hash)
                values.put("digits", otpauth.digits)
                val result = db.update("otpauth", values,
                    "id=?", arrayOf(otpauth.toString()))
                if (result != 1) {
                    return ERR_RES_COUNT
                }
            } catch (_: SQLException) {
                return ERR_SQL_EXCEPT
            }
        }
        return OK
    }

    fun deleteSecret(id: Int): Int {
        // Функция удаляет запись OTPauth в БД.
        // Поиск записи идёт по полю id.
        // Возвращает 0, если успешно и <> 0, если нет.
        if (id !in appSecrets.map(OTPauth::id)) {
            return ERR_LIST_COUNT
        }
        writableDatabase.use { db ->
            try {
                val result = db.delete("otpauth",
                    "id=?", arrayOf(id.toString()))
                if (result != 1) {
                    return ERR_RES_COUNT
                }
            } catch (_: SQLException) {
                return ERR_SQL_EXCEPT
            }
        }
        return OK
    }

}