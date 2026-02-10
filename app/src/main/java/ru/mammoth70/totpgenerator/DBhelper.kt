package ru.mammoth70.totpgenerator

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ru.mammoth70.totpgenerator.App.Companion.appContext
import java.sql.SQLException

class DBhelper(context: Context?) : SQLiteOpenHelper(context, "totpDB",
        null, DB_VERSION) {
    // Класс обслуживает базу данных со списком OTPauth.
    // Поле secret в таблице otpauth хранится зашифрованным, в поле iv лежит инициализационный вектор.

    companion object {
        private const val ERR_SQL_EXCEPT = -100L
        private const val ERR_CRYPTO = -50L
        private const val ERR_RES_COUNT = -1L

        private const val DB_VERSION = 1 // версия БД
        val dbHelper = DBhelper(appContext)
        private val CREATE_TABLE_SECRETS = """
            CREATE TABLE IF NOT EXISTS otpauth 
            (id integer PRIMARY KEY AUTOINCREMENT, 
            label text UNIQUE NOT NULL, issuer text DEFAULT '', 
            secret text UNIQUE NOT NULL, iv NOT NULL, 
            step integer DEFAULT 30, hash text DEFAULT 'SHA1', digits integer DEFAULT 6);
        """.trimIndent()
    }


    override fun onOpen(db: SQLiteDatabase) {
        // Функция включает Write-Ahead Logging для стабильности. (minSdk 30).

        super.onOpen(db)
        db.enableWriteAheadLogging()
    }


    override fun onCreate(db: SQLiteDatabase) {
        // Функция создаёт таблицу OTPauth.

        db.execSQL(CREATE_TABLE_SECRETS)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Функция делает апгрейд БД.
        // Пока, это заглушка.
    }


    fun readAllDbSecrets(): List<OTPauth> {
        // Функция считывает и возвращает все OTPauth.

        val secretList = mutableListOf<OTPauth>()

        readableDatabase.rawQuery("SELECT * FROM otpauth ORDER BY id;", null).use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow("id")
            val labelIdx = cursor.getColumnIndexOrThrow("label")
            val issuerIdx = cursor.getColumnIndexOrThrow("issuer")
            val secretIdx = cursor.getColumnIndexOrThrow("secret")
            val ivIdx = cursor.getColumnIndexOrThrow("iv")
            val stepIdx = cursor.getColumnIndexOrThrow("step")
            val hashIdx = cursor.getColumnIndexOrThrow("hash")
            val digitsIdx = cursor.getColumnIndexOrThrow("digits")

            while (cursor.moveToNext()) {
                val encryptedPair = StringPair(
                    encodedText = cursor.getString(secretIdx),
                    iv = cursor.getString(ivIdx)
                )

                val secret = OTPauth(
                    id = cursor.getLong(idIdx),
                    label = cursor.getString(labelIdx),
                    issuer = cursor.getString(issuerIdx),
                    secret = decryptString(encryptedPair),
                    period = cursor.getInt(stepIdx),
                    hash = cursor.getString(hashIdx),
                    digits = cursor.getInt(digitsIdx)
                )
                secretList.add(secret)
            }
        }
        return secretList
    }


    fun addDbSecret(otpauth: OTPauth): Long  {
        // Функция добавляет запись OTPauth в БД.
        // Если добавлено успешно - возвращает 0, если не успешно - возвращает не 0.

        val pair = encryptString(otpauth.secret)
        if (pair.encodedText.isEmpty() || pair.iv.isEmpty()) {
            return ERR_CRYPTO
        }

        return try {
            val values = ContentValues().apply {
                put("label", otpauth.label)
                put("issuer", otpauth.issuer)
                put("secret", pair.encodedText)
                put("iv", pair.iv)
                put("step", otpauth.period)
                put("hash", otpauth.hash)
                put("digits", otpauth.digits)
            }

            val result = writableDatabase.insert("otpauth", null, values)
            if (result != -1L) result else ERR_RES_COUNT

        } catch (e: SQLException) {
            LogSmart.e("DBhelper", "SQLException в addSecret($otpauth)", e)
            ERR_SQL_EXCEPT
        }
    }


    fun editDbSecret(otpauth: OTPauth): Long {
        // Функция обновляет запись OTPauth в БД.
        // Поиск записи идёт по полю id.
        // Если обновлено успешно - возвращает 0, если не успешно - возвращает не 0.

        val pair = encryptString(otpauth.secret)
        if (pair.encodedText.isEmpty() || pair.iv.isEmpty()) {
            return ERR_CRYPTO
        }

        return try {
            val values = ContentValues().apply {
                put("label", otpauth.label)
                put("issuer", otpauth.issuer)
                put("secret", pair.encodedText)
                put("iv", pair.iv)
                put("step", otpauth.period)
                put("hash", otpauth.hash)
                put("digits", otpauth.digits)
            }

            val result = writableDatabase.update(
                "otpauth",
                values,
                "id = ?",
                arrayOf(otpauth.id.toString())
            )

            if (result == 1) 1L else ERR_RES_COUNT

        } catch (e: SQLException) {
            LogSmart.e("DBhelper", "SQLException в editSecret($otpauth)", e)
            ERR_SQL_EXCEPT
        }
    }


    fun deleteDbSecret(id: Long): Long {
        // Функция удаляет запись OTPauth в БД.
        // Поиск записи идёт по полю id.
        // Если удалено успешно - возвращает 0, если не успешно - возвращает не 0.

        return try {
            val result = writableDatabase.delete(
                "otpauth",
                "id = ?",
                arrayOf(id.toString())
            )

            if (result == 1) 1L else ERR_RES_COUNT

        } catch (e: SQLException) {
            LogSmart.e("DBhelper", "SQLException в deleteSecret($id)", e)
            ERR_SQL_EXCEPT
        }
    }

}