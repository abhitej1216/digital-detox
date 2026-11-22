package com.example.digitaldetox.service

import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

enum class AccessStatus {
    ALLOWED_TO_REQUEST, // No cooldown active and daily quota remaining; can request session via One Sec
    ACTIVE_SESSION,     // Currently inside the active micro-session window (e.g. within 5 minutes)
    IN_COOLDOWN,        // Micro-session expired; mandatory cooldown window active
    DAILY_LIMIT_REACHED // Daily open limit reached for this app today
}

data class MicroSessionState(
    val activeSessionUntil: Long = 0L,
    val cooldownUntil: Long = 0L,
    val dailyOpensCount: Int = 0,
    val lastOpenDateMillis: Long = 0L
)

/**
 * Manages Micro-Sessions, Mandatory Cooldown Windows, and Daily Open Limits per app.
 */
object MindfulInterventionManager {

    private val sessionsMap = ConcurrentHashMap<String, MicroSessionState>()

    /**
     * Checks the current access status for [packageName].
     */
    fun checkAccessStatus(
        packageName: String,
        dailyLimit: Int = 5
    ): AccessStatus {
        val now = System.currentTimeMillis()
        val current = sessionsMap[packageName] ?: return AccessStatus.ALLOWED_TO_REQUEST

        // Check if day has changed to reset daily open counts
        val isToday = isSameDay(current.lastOpenDateMillis, now)
        val dailyCount = if (isToday) current.dailyOpensCount else 0

        // 1. Check if currently inside active micro-session
        if (now < current.activeSessionUntil) {
            return AccessStatus.ACTIVE_SESSION
        }

        // 2. Check if currently in mandatory cooldown
        if (now < current.cooldownUntil) {
            return AccessStatus.IN_COOLDOWN
        }

        // 3. Check if daily open limit reached (dailyLimit > 0 means limited, 0 = unlimited)
        if (dailyLimit in 1..dailyCount) {
            return AccessStatus.DAILY_LIMIT_REACHED
        }

        return AccessStatus.ALLOWED_TO_REQUEST
    }

    /**
     * Starts a new micro-session for [packageName] with specified duration and cooldown.
     */
    fun startMicroSession(
        packageName: String,
        sessionMinutes: Int = 5,
        cooldownMinutes: Int = 30
    ) {
        val now = System.currentTimeMillis()
        val current = sessionsMap[packageName]
        val isToday = current != null && isSameDay(current.lastOpenDateMillis, now)
        val newDailyCount = if (isToday) current!!.dailyOpensCount + 1 else 1

        val sessionDurationMs = sessionMinutes * 60 * 1000L
        val cooldownDurationMs = cooldownMinutes * 60 * 1000L

        val sessionExpiresAt = now + sessionDurationMs
        val cooldownExpiresAt = sessionExpiresAt + cooldownDurationMs

        sessionsMap[packageName] = MicroSessionState(
            activeSessionUntil = sessionExpiresAt,
            cooldownUntil = cooldownExpiresAt,
            dailyOpensCount = newDailyCount,
            lastOpenDateMillis = now
        )
    }

    /**
     * Check if [packageName] is currently within its active micro-session.
     */
    fun hasTemporaryAccess(packageName: String): Boolean {
        val current = sessionsMap[packageName] ?: return false
        return System.currentTimeMillis() < current.activeSessionUntil
    }

    /**
     * Returns remaining active session time in seconds.
     */
    fun getRemainingSessionSeconds(packageName: String): Long {
        val current = sessionsMap[packageName] ?: return 0L
        val diff = current.activeSessionUntil - System.currentTimeMillis()
        return if (diff > 0) diff / 1000L else 0L
    }

    /**
     * Returns remaining cooldown time in seconds.
     */
    fun getRemainingCooldownSeconds(packageName: String): Long {
        val current = sessionsMap[packageName] ?: return 0L
        val diff = current.cooldownUntil - System.currentTimeMillis()
        return if (diff > 0) diff / 1000L else 0L
    }

    /**
     * Returns how many times [packageName] has been opened today.
     */
    fun getDailyOpensToday(packageName: String): Int {
        val current = sessionsMap[packageName] ?: return 0
        val isToday = isSameDay(current.lastOpenDateMillis, System.currentTimeMillis())
        return if (isToday) current.dailyOpensCount else 0
    }

    /**
     * Returns all apps currently in cooldown or active sessions.
     */
    fun getActiveCooldowns(): Map<String, MicroSessionState> {
        val now = System.currentTimeMillis()
        return sessionsMap.filter { (_, state) -> now < state.cooldownUntil }
    }

    /**
     * Revoke any granted temporary access for [packageName].
     */
    fun revokeAccess(packageName: String) {
        sessionsMap.remove(packageName)
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        if (time1 == 0L || time2 == 0L) return false
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
