package ru.mammoth70.totpgenerator

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [OTPauthEntity::class], version = 2, exportSchema = false)
abstract class OTPauthDataBase : RoomDatabase() {
    // Класс, описывающий базу данных приложения.

    abstract fun otpDao(): OTPauthDao
    // Метод доступа к DAO.

    companion object {
        private const val DB_NAME = "totpDB"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Функция миграции с предыдущей версии БД на версию БД, описываемую Room.

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `otpauth_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `label` TEXT NOT NULL, 
                    `issuer` TEXT, 
                    `secret` TEXT NOT NULL, 
                    `iv` TEXT NOT NULL, 
                    `period` INTEGER, 
                    `hash` TEXT, 
                    `digits` INTEGER
                    )
                    """.trimIndent())

                db.execSQL("""
                    INSERT INTO otpauth_new (id, label, issuer, secret, iv, period, hash, digits)
                    SELECT id, label, issuer, secret, iv, step, hash, digits FROM otpauth
                    """.trimIndent())

                db.execSQL("DROP TABLE otpauth")

                db.execSQL("ALTER TABLE otpauth_new RENAME TO otpauth")

                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_otpauth_label` ON `otpauth` (`label`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_otpauth_secret` ON `otpauth` (`secret`)")

            }
        }

        @Volatile
        private var INSTANCE: OTPauthDataBase? = null
        // Ссылка на объект базы данных.

        fun getInstance(context: Context): OTPauthDataBase {
            // Функция создаёт INSTANS, гарантируя, что только один поток создаст экземпляр БД,
            // даже если несколько потоков вызовут метод одновременно.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OTPauthDataBase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
