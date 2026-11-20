package com.example.digitaldetox.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.digitaldetox.data.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Query("SELECT value FROM settings WHERE key = :key LIMIT 1")
    suspend fun getStringValue(key: String): String?

    @Query("SELECT value FROM settings WHERE key = :key LIMIT 1")
    fun getStringValueFlow(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: SettingsEntity)

    @Query("UPDATE settings SET value = :value WHERE key = :key")
    suspend fun updateValue(key: String, value: String)

    @Query("DELETE FROM settings WHERE key = :key")
    suspend fun delete(key: String)
}