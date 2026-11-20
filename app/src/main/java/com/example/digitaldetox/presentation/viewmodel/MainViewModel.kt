package com.example.digitaldetox.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    fun refreshData() {
        viewModelScope.launch {
            // TODO: Load data from repositories
        }
    }
}

data class MainUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)