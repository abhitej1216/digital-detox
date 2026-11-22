package com.example.digitaldetox.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitaldetox.data.repository.SettingsRepository
import com.example.digitaldetox.util.BatteryOptimizationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun checkBatteryOptimizationStatus() {
        val isIgnored = BatteryOptimizationHelper.isBatteryOptimizationIgnored(context)
        _uiState.update { it.copy(isBatteryOptimizationIgnored = isIgnored) }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val oneSecEnabled = repository.getBoolean("one_sec_enabled", true)
            val microSessionMinutes = repository.getInt("micro_session_minutes", 5)
            val cooldownMinutes = repository.getInt("cooldown_minutes", 30)
            val dailyOpenLimit = repository.getInt("daily_open_limit", 5)

            val nightMode = repository.getBoolean("night_mode", false)
            val studyTarget = repository.getInt("study_target", 120)
            val workTarget = repository.getInt("work_target", 240)
            val blockingMethod = repository.getString("blocking_method", "Strict")
            val strictUnblockProtection = repository.getBoolean("strict_unblock_protection", true)
            val isBatteryOptimizationIgnored = BatteryOptimizationHelper.isBatteryOptimizationIgnored(context)

            _uiState.update {
                it.copy(
                    oneSecEnabled = oneSecEnabled,
                    microSessionMinutes = microSessionMinutes,
                    cooldownMinutes = cooldownMinutes,
                    dailyOpenLimit = dailyOpenLimit,
                    nightMode = nightMode,
                    studyTarget = studyTarget.toString(),
                    workTarget = workTarget.toString(),
                    blockingMethod = blockingMethod,
                    strictUnblockProtection = strictUnblockProtection,
                    isBatteryOptimizationIgnored = isBatteryOptimizationIgnored,
                    isLoading = false
                )
            }
        }
    }

    fun onOneSecEnabledChanged(enabled: Boolean) {
        _uiState.update { it.copy(oneSecEnabled = enabled) }
        viewModelScope.launch { repository.saveBoolean("one_sec_enabled", enabled) }
    }

    fun onMicroSessionMinutesChanged(minutes: Int) {
        _uiState.update { it.copy(microSessionMinutes = minutes) }
        viewModelScope.launch { repository.saveInt("micro_session_minutes", minutes) }
    }

    fun onCooldownMinutesChanged(minutes: Int) {
        _uiState.update { it.copy(cooldownMinutes = minutes) }
        viewModelScope.launch { repository.saveInt("cooldown_minutes", minutes) }
    }

    fun onDailyOpenLimitChanged(limit: Int) {
        _uiState.update { it.copy(dailyOpenLimit = limit) }
        viewModelScope.launch { repository.saveInt("daily_open_limit", limit) }
    }

    fun onNightModeChanged(enabled: Boolean) {
        _uiState.update { it.copy(nightMode = enabled) }
        viewModelScope.launch { repository.saveBoolean("night_mode", enabled) }
    }

    fun onStrictUnblockProtectionChanged(enabled: Boolean) {
        _uiState.update { it.copy(strictUnblockProtection = enabled) }
        viewModelScope.launch { repository.saveBoolean("strict_unblock_protection", enabled) }
    }

    fun onStudyTargetChanged(target: String) {
        _uiState.update { it.copy(studyTarget = target) }
        target.toIntOrNull()?.let {
            viewModelScope.launch { repository.saveInt("study_target", it) }
        }
    }

    fun onWorkTargetChanged(target: String) {
        _uiState.update { it.copy(workTarget = target) }
        target.toIntOrNull()?.let {
            viewModelScope.launch { repository.saveInt("work_target", it) }
        }
    }

    fun onBlockingMethodChanged(method: String) {
        _uiState.update { it.copy(blockingMethod = method) }
        viewModelScope.launch { repository.saveString("blocking_method", method) }
    }
}

data class SettingsUiState(
    val oneSecEnabled: Boolean = true,
    val microSessionMinutes: Int = 5,
    val cooldownMinutes: Int = 30,
    val dailyOpenLimit: Int = 5,
    val nightMode: Boolean = false,
    val studyTarget: String = "120",
    val workTarget: String = "240",
    val blockingMethod: String = "Strict",
    val strictUnblockProtection: Boolean = true,
    val isBatteryOptimizationIgnored: Boolean = true,
    val isLoading: Boolean = true
)
