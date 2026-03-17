package com.example.devflow.ui.contests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.devflow.data.model.Contest
import com.example.devflow.data.repository.ContestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ContestUiState {
    object Loading : ContestUiState()
    data class Success(val contests: List<Contest>) : ContestUiState()
    data class Error(val message: String) : ContestUiState()
}

class ContestViewModel : ViewModel() {

    private val repository = ContestRepository()

    private val _uiState = MutableStateFlow<ContestUiState>(ContestUiState.Loading)
    val uiState: StateFlow<ContestUiState> = _uiState

    init {
        fetchContests()
    }

    fun fetchContests() {
        viewModelScope.launch {
            _uiState.value = ContestUiState.Loading
            try {
                val contests = repository.getUpcomingContests()
                _uiState.value = ContestUiState.Success(contests)
            } catch (e: Exception) {
                _uiState.value = ContestUiState.Error("Failed to load contests")
            }
        }
    }
}