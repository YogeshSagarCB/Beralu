package com.example.beralu.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beralu.domain.repository.BeraluRepository
import com.example.beralu.ui.main.MainScreenUiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    dataRepository: BeraluRepository
) : ViewModel() {
  val uiState: StateFlow<MainScreenUiState> =
    dataRepository.getAllNotes()
      .map<List<com.example.beralu.domain.model.BeraluNote>, MainScreenUiState> { Success(it) }
      .catch { emit(MainScreenUiState.Error(it)) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainScreenUiState.Loading)
}

sealed interface MainScreenUiState {
  object Loading : MainScreenUiState

  data class Error(val throwable: Throwable) : MainScreenUiState

  data class Success(val data: List<com.example.beralu.domain.model.BeraluNote>) : MainScreenUiState
}

