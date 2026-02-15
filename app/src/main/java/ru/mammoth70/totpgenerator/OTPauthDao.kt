package ru.mammoth70.totpgenerator

import androidx.room.*

@Dao
interface OTPauthDao {
    // Объект доступа к данным. (DAO).
    // Интерфейс содержит функции для каждой операции с базой данных.

    @Query("SELECT * FROM otpauth ORDER BY id ASC")
    suspend fun getAll(): List<OTPauthEntity>
    // Получить все OTPauth.

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: OTPauthEntity): Long
    // Добавить OTPauth, в случае удачи вернуть его ID.

    @Update
    suspend fun update(entity: OTPauthEntity): Int
    // Обновить OTPauth, в случае удачи вернуть кол-во фактически обновлённых строк.

    @Query("DELETE FROM otpauth WHERE id = :targetId")
    suspend fun deleteById(targetId: Long): Int
    // Удалить OTPauth, в случае удачи вернуть 1, в случае неудачи вернуть 0.
}