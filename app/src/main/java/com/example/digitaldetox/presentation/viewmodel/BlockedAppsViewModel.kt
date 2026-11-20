package com.example.digitaldetox.presentation.viewmodel

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitaldetox.data.repository.AppRepository
import com.example.digitaldetox.data.repository.SettingsRepository
import com.example.digitaldetox.domain.model.App
import com.example.digitaldetox.service.AppBlockingService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FilterMode {
    ALL,
    BLOCKED_ONLY
}

data class PendingUnblockApp(
    val packageName: String,
    val label: String
)

@HiltViewModel
class BlockedAppsViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlockedAppsUiState())
    val uiState: StateFlow<BlockedAppsUiState> = _uiState.asStateFlow()

    private var allApps: List<App> = emptyList()

    init {
        viewModelScope.launch {
            appRepository.syncInstalledApps()
            loadApps()
        }
        viewModelScope.launch {
            settingsRepository.getBooleanFlow("strict_unblock_protection", true).collect { enabled ->
                _uiState.update { it.copy(isStrictProtectionEnabled = enabled) }
            }
        }
        checkAccessibilityService()
    }

    fun checkAccessibilityService() {
        val enabled = isAccessibilityServiceEnabled(context)
        _uiState.update { it.copy(isAccessibilityServiceEnabled = enabled) }
    }

    private fun loadApps() {
        viewModelScope.launch {
            allApps = appRepository.getAllApps()
            _uiState.update { it.copy(isLoading = false) }
            applyFilter()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilter()
    }

    fun onFilterModeChanged(mode: FilterMode) {
        _uiState.update { it.copy(filterMode = mode) }
        applyFilter()
    }

    private fun applyFilter() {
        val query = _uiState.value.searchQuery.trim()
        val mode = _uiState.value.filterMode

        val filteredApps = allApps.filter { app ->
            val matchesQuery = query.isEmpty() || app.label.contains(query, ignoreCase = true)
            val matchesFilter = when (mode) {
                FilterMode.ALL -> true
                FilterMode.BLOCKED_ONLY -> app.blockEnabled
            }
            matchesQuery && matchesFilter
        }

        _uiState.update { it.copy(apps = filteredApps) }
    }

    fun toggleAppBlocked(packageName: String, enabled: Boolean) {
        if (!enabled && _uiState.value.isStrictProtectionEnabled) {
            val appLabel = allApps.find { it.packageName == packageName }?.label ?: packageName
            _uiState.update {
                it.copy(pendingUnblockApp = PendingUnblockApp(packageName, appLabel))
            }
        } else {
            viewModelScope.launch {
                appRepository.toggleAppBlocked(packageName, enabled)
                allApps = appRepository.getAllApps()
                applyFilter()
            }
        }
    }

    fun cancelUnblock() {
        _uiState.update { it.copy(pendingUnblockApp = null) }
    }

    fun confirmUnblockApp() {
        val pending = _uiState.value.pendingUnblockApp ?: return
        _uiState.update { it.copy(pendingUnblockApp = null) }
        viewModelScope.launch {
            appRepository.toggleAppBlocked(pending.packageName, false)
            allApps = appRepository.getAllApps()
            applyFilter()
        }
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, AppBlockingService::class.java)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName) {
                return true
            }
        }
        return false
    }
}

data class BlockedAppsUiState(
    val apps: List<App> = emptyList(),
    val searchQuery: String = "",
    val filterMode: FilterMode = FilterMode.ALL,
    val isLoading: Boolean = true,
    val isAccessibilityServiceEnabled: Boolean = false,
    val isStrictProtectionEnabled: Boolean = true,
    val pendingUnblockApp: PendingUnblockApp? = null
)
