package com.example.digitaldetox.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.digitaldetox.data.entity.BlockedSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedSessionDao {

    @Query("SELECT * FROM blocked_sessions WHERE packageName = :packageName")
    fun getBlockedSessions(packageName: String): Flow<List<BlockedSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: BlockedSessionEntity)

    @Query("DELETE FROM blocked_sessions WHERE packageName = :packageName AND timestamp < :before")
    suspend fun deleteOldSessions(packageName: String, before: Long)

    @Query("SELECT * FROM blocked_sessions ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSessions(limit: Int): List<BlockedSessionEntity>
}