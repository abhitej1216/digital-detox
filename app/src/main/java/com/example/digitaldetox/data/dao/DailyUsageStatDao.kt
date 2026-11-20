package com.example.digitaldetox.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.digitaldetox.data.entity.DailyUsageStatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyUsageStatDao {

    @Query("SELECT * FROM daily_usage_stats WHERE date = :date")
    suspend fun getByDate(date: String): DailyUsageStatEntity?

    @Query("SELECT * FROM daily_usage_stats ORDER BY date DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<DailyUsageStatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stat: DailyUsageStatEntity)

    @Query("DELETE FROM daily_usage_stats WHERE date < :before")
    suspend fun deleteOldStats(before: String)
}