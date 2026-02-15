package ru.mammoth70.totpgenerator

import androidx.room.*

@Dao
interface OTPauthDao {
    @Query("SELECT * FROM otpauth ORDER BY id ASC")
    suspend fun getAll(): List<OTPauthEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: OTPauthEntity): Long

    @Update
    suspend fun update(entity: OTPauthEntity): Int

    @Query("DELETE FROM otpauth WHERE id = :targetId")
    suspend fun deleteById(targetId: Long): Int
}