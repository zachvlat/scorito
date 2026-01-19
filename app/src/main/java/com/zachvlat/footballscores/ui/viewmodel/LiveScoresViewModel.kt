package com.zachvlat.footballscores.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zachvlat.footballscores.data.model.LiveScoresResponse
import com.zachvlat.footballscores.data.repository.LiveScoresRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LiveScoresViewModel(private val repository: LiveScoresRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow<LiveScoresUiState>(LiveScoresUiState.Loading)
    val uiState: StateFlow<LiveScoresUiState> = _uiState.asStateFlow()
    
    init {
        loadTodayScores()
    }
    
    fun loadTodayScores() {
        viewModelScope.launch {
            _uiState.value = LiveScoresUiState.Loading
            
            repository.getTodayLiveScores().fold(
                onSuccess = { response ->
                    _uiState.value = LiveScoresUiState.Success(response)
                },
                onFailure = { error ->
                    _uiState.value = LiveScoresUiState.Error(
                        message = error.message ?: "Unknown error occurred"
                    )
                }
            )
        }
    }
    
    fun loadScoresForDate(dateString: String) {
        viewModelScope.launch {
            _uiState.value = LiveScoresUiState.Loading
            
            repository.getLiveScoresForDate(dateString).fold(
                onSuccess = { response ->
                    _uiState.value = LiveScoresUiState.Success(response)
                },
                onFailure = { error ->
                    _uiState.value = LiveScoresUiState.Error(
                        message = error.message ?: "Failed to load scores for selected date"
                    )
                }
            )
        }
    }
    
    fun refresh() {
        when (val currentState = _uiState.value) {
            is LiveScoresUiState.Success -> {
                loadTodayScores()
            }
            else -> {
                loadTodayScores()
            }
        }
    }
    
    fun getTodayDateString(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        return dateFormat.format(Date())
    }
    
    fun getYesterdayDateString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        return dateFormat.format(calendar.time)
    }
    
    fun getTomorrowDateString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        return dateFormat.format(calendar.time)
    }
}

sealed class LiveScoresUiState {
    object Loading : LiveScoresUiState()
    data class Success(val response: LiveScoresResponse) : LiveScoresUiState()
    data class Error(val message: String) : LiveScoresUiState()
}

class LiveScoresViewModelFactory(private val repository: LiveScoresRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LiveScoresViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LiveScoresViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}