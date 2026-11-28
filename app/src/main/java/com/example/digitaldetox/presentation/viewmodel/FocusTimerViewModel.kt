package com.example.digitaldetox.presentation.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitaldetox.data.dao.FocusSessionDao
import com.example.digitaldetox.data.entity.FocusMode
import com.example.digitaldetox.data.entity.FocusSessionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FocusTimerUiState(
    val selectedMode: FocusMode = FocusMode.STUDY,
    val durationMinutes: Int = 25,
    val remainingTimeSeconds: Long = 25 * 60L,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val sessionStartTime: Long = 0L
)

@HiltViewModel
class FocusTimerViewModel @Inject constructor(
    private val focusSessionDao: FocusSessionDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusTimerUiState())
    val uiState: StateFlow<FocusTimerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var runStartElapsedMs: Long = 0L
    private var targetEndElapsedMs: Long = 0L
    private var accumulatedFocusMs: Long = 0L

    fun selectMode(mode: FocusMode) {
        if (!_uiState.value.isRunning && !_uiState.value.isPaused) {
            _uiState.update { it.copy(selectedMode = mode) }
        }
    }

    fun setDuration(minutes: Int) {
        if (!_uiState.value.isRunning && !_uiState.value.isPaused) {
            val validMinutes = minutes.coerceIn(1, 180)
            _uiState.update {
                it.copy(
                    durationMinutes = validMinutes,
                    remainingTimeSeconds = validMinutes * 60L
                )
            }
        }
    }

    fun startTimer() {
        if (_uiState.value.isRunning) return

        val nowEpoch = System.currentTimeMillis()
        val nowElapsed = SystemClock.elapsedRealtime()
        if (!_uiState.value.isPaused) {
            accumulatedFocusMs = 0L
            _uiState.update { it.copy(sessionStartTime = nowEpoch) }
        }

        runStartElapsedMs = nowElapsed
        targetEndElapsedMs = nowElapsed + (_uiState.value.remainingTimeSeconds * 1000L)
        _uiState.update { it.copy(isRunning = true, isPaused = false) }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isRunning) {
                val remainingMs = targetEndElapsedMs - SystemClock.elapsedRealtime()
                val nextRemainingSeconds = if (remainingMs > 0L) (remainingMs + 999L) / 1000L else 0L

                _uiState.update { current ->
                    if (nextRemainingSeconds <= 0L) {
                        current.copy(remainingTimeSeconds = 0L, isRunning = false, isPaused = false)
                    } else {
                        current.copy(remainingTimeSeconds = nextRemainingSeconds)
                    }
                }

                if (nextRemainingSeconds <= 0L) {
                    onTimerCompleted()
                    break
                }
                delay(250L)
            }
        }
    }

    fun pauseTimer() {
        if (!_uiState.value.isRunning) return
        timerJob?.cancel()
        val nowElapsed = SystemClock.elapsedRealtime()
        if (runStartElapsedMs > 0L) {
            accumulatedFocusMs += (nowElapsed - runStartElapsedMs).coerceAtLeast(0L)
            runStartElapsedMs = 0L
        }
        val remainingMs = (targetEndElapsedMs - nowElapsed).coerceAtLeast(0L)
        val remainingSecs = (remainingMs + 999L) / 1000L
        _uiState.update {
            it.copy(
                isRunning = false,
                isPaused = true,
                remainingTimeSeconds = remainingSecs
            )
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        val currentState = _uiState.value
        val nowEpoch = System.currentTimeMillis()
        val nowElapsed = SystemClock.elapsedRealtime()

        val activeSegmentMs = if (currentState.isRunning && runStartElapsedMs > 0L) {
            (nowElapsed - runStartElapsedMs).coerceAtLeast(0L)
        } else 0L
        val totalFocusedMs = accumulatedFocusMs + activeSegmentMs

        if (currentState.sessionStartTime > 0L && totalFocusedMs >= 60_000L) {
            val elapsedMinutes = (totalFocusedMs / 60_000L).toInt().coerceAtLeast(1)
            saveSession(
                mode = currentState.selectedMode,
                startTime = currentState.sessionStartTime,
                endTime = nowEpoch,
                durationMinutes = elapsedMinutes
            )
        }

        resetTimerState()
    }

    private fun onTimerCompleted() {
        val currentState = _uiState.value
        val now = System.currentTimeMillis()
        val startTime = if (currentState.sessionStartTime > 0L) {
            currentState.sessionStartTime
        } else {
            now - currentState.durationMinutes * 60_000L
        }

        saveSession(
            mode = currentState.selectedMode,
            startTime = startTime,
            endTime = now,
            durationMinutes = currentState.durationMinutes
        )

        resetTimerState()
    }

    private fun saveSession(mode: FocusMode, startTime: Long, endTime: Long, durationMinutes: Int) {
        viewModelScope.launch {
            val session = FocusSessionEntity(
                mode = mode,
                startTime = startTime,
                endTime = endTime,
                goalMinutes = durationMinutes,
                durationMinutes = durationMinutes,
                completed = true
            )
            focusSessionDao.insert(session)
        }
    }

    private fun resetTimerState() {
        runStartElapsedMs = 0L
        targetEndElapsedMs = 0L
        accumulatedFocusMs = 0L
        _uiState.update { current ->
            current.copy(
                isRunning = false,
                isPaused = false,
                remainingTimeSeconds = current.durationMinutes * 60L,
                sessionStartTime = 0L
            )
        }
    }
}
