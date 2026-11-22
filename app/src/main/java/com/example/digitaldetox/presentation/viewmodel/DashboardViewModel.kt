package com.example.digitaldetox.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitaldetox.data.dao.DailyUsageStatDao
import com.example.digitaldetox.data.dao.FocusSessionDao
import com.example.digitaldetox.data.repository.SettingsRepository
import com.example.digitaldetox.data.repository.UsageStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val focusSessionDao: FocusSessionDao,
    private val dailyUsageStatDao: DailyUsageStatDao,
    private val usageStatsRepository: UsageStatsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            val hasPermission = usageStatsRepository.hasUsageStatsPermission()
            val todayStart = getStartOfDay()
            val studyTime = focusSessionDao.getTotalFocusMinutes("Study", todayStart) ?: 0
            val workTime = focusSessionDao.getTotalFocusMinutes("Work", todayStart) ?: 0
            val focusTime = studyTime + workTime

            val studyTarget = settingsRepository.getInt("study_target", 120)
            val workTarget = settingsRepository.getInt("work_target", 240)
            val totalTarget = studyTarget + workTarget

            val goalProgress = if (totalTarget > 0) {
                ((studyTime + workTime) / totalTarget.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }

            val actualUsageMinutes = if (hasPermission) {
                val stats = usageStatsRepository.getTodayUsageStats()
                stats.firstOrNull()?.totalAppUsageMinutes ?: 0
            } else {
                0
            }

            val mindfulSavesCount = settingsRepository.getInt("mindful_saves_count", 0)

            _uiState.update {
                it.copy(
                    hasPermission = hasPermission,
                    studyTime = studyTime,
                    workTime = workTime,
                    totalFocusTime = if (hasPermission && actualUsageMinutes > 0) actualUsageMinutes else focusTime,
                    totalAppUsageTime = actualUsageMinutes,
                    goalProgress = goalProgress,
                    mindfulSavesCount = mindfulSavesCount,
                    isLoading = false
                )
            }
        }
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

data class DashboardUiState(
    val totalFocusTime: Int = 0,
    val studyTime: Int = 0,
    val workTime: Int = 0,
    val totalAppUsageTime: Int = 0,
    val goalProgress: Float = 0f,
    val mindfulSavesCount: Int = 0,
    val hasPermission: Boolean = false,
    val isLoading: Boolean = true
)
