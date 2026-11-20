package com.example.digitaldetox.domain.model

data class FocusSession(
    val id: Long = 0,
    val mode: FocusMode,
    val startTime: Long,
    val endTime: Long,
    val goalMinutes: Int
)

enum class FocusMode {
    STUDY,
    WORK
}