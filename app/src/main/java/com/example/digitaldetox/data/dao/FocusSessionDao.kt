package com.example.digitaldetox.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.digitaldetox.data.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Query("SELECT * FROM focus_sessions WHERE mode = :mode AND startTime > :startTime")
    fun getSessionsByMode(mode: String, startTime: Long): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: FocusSessionEntity): Long

    @Query("SELECT SUM(goalMinutes) FROM focus_sessions WHERE mode = :mode AND startTime > :startTime")
    suspend fun getTotalFocusMinutes(mode: String, startTime: Long): Int?

    @Query("SELECT * FROM focus_sessions WHERE startTime > :startTime ORDER BY startTime DESC")
    suspend fun getActiveSessions(startTime: Long): List<FocusSessionEntity>
}