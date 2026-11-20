package com.example.digitaldetox.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mode: FocusMode,
    val startTime: Long,
    val endTime: Long,
    val goalMinutes: Int,
    val durationMinutes: Int = goalMinutes,
    val completed: Boolean = true
)

enum class FocusMode {
    STUDY,
    WORK
}