package com.example.digitaldetox.presentation.viewmodel

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

        val now = System.currentTimeMillis()
        if (!_uiState.value.isPaused) {
            _uiState.update { it.copy(sessionStartTime = now) }
        }

        _uiState.update { it.copy(isRunning = true, isPaused = false) }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingTimeSeconds > 0 && _uiState.value.isRunning) {
                delay(1000L)
                _uiState.update { current ->
                    val nextRemaining = current.remainingTimeSeconds - 1
                    if (nextRemaining <= 0) {
                        current.copy(remainingTimeSeconds = 0L, isRunning = false, isPaused = false)
                    } else {
                        current.copy(remainingTimeSeconds = nextRemaining)
                    }
                }
            }

            if (_uiState.value.remainingTimeSeconds == 0L) {
                onTimerCompleted()
            }
        }
    }

    fun pauseTimer() {
        if (!_uiState.value.isRunning) return
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false, isPaused = true) }
    }

    fun stopTimer() {
        timerJob?.cancel()
        val currentState = _uiState.value
        val now = System.currentTimeMillis()

        if (currentState.sessionStartTime > 0L && (currentState.isRunning || currentState.isPaused)) {
            val elapsedMs = now - currentState.sessionStartTime
            val elapsedMinutes = maxOf(1, (elapsedMs / 60000).toInt())
            saveSession(
                mode = currentState.selectedMode,
                startTime = currentState.sessionStartTime,
                endTime = now,
                durationMinutes = elapsedMinutes
            )
        }

        resetTimerState()
    }

    private fun onTimerCompleted() {
        val currentState = _uiState.value
        val now = System.currentTimeMillis()
        val startTime = if (currentState.sessionStartTime > 0L) currentState.sessionStartTime else (now - currentState.durationMinutes * 60000L)

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
