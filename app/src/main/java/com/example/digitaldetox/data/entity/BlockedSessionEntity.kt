package com.example.digitaldetox.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_sessions")
data class BlockedSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val startTime: Long,
    val endTime: Long,
    val timestamp: Long = System.currentTimeMillis()
)