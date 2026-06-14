package com.example.beralu.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beralu.domain.model.BeraluContext
import com.example.beralu.domain.model.BeraluNote
import com.example.beralu.domain.model.BeraluSubContext
import com.example.beralu.domain.repository.BeraluRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val dataRepository: BeraluRepository
) : ViewModel() {
    val uiState: StateFlow<MainScreenUiState> = combine(
        dataRepository.getContexts(),
        dataRepository.getAllSubContexts(),
        dataRepository.getAllNotes()
    ) { contexts, subContexts, notes ->
        val data = contexts.map { ctx ->
            UnifiedContext(
                context = ctx,
                notes = notes.filter { it.contextId == ctx.id && it.subContextId == null },
                subContexts = subContexts.filter { it.contextId == ctx.id }.map { sub ->
                    UnifiedSubContext(
                        subContext = sub,
                        notes = notes.filter { it.subContextId == sub.id }
                    )
                }
            )
        }
        MainScreenUiState.Success(data) as MainScreenUiState
    }.catch { 
        emit(MainScreenUiState.Error(it)) 
    }.stateIn(
        viewModelScope, 
        SharingStarted.WhileSubscribed(5000), 
        MainScreenUiState.Loading
    )

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            dataRepository.deleteNote(noteId)
        }
    }

    fun deleteContext(contextId: String) {
        viewModelScope.launch {
            dataRepository.deleteContext(contextId)
        }
    }

    fun deleteNotesByContext(contextId: String) {
        viewModelScope.launch {
            dataRepository.deleteNotesByContext(contextId)
        }
    }

    fun deleteSubContext(subContextId: String) {
        viewModelScope.launch {
            dataRepository.deleteSubContext(subContextId)
        }
    }
}

data class UnifiedContext(
    val context: BeraluContext,
    val notes: List<BeraluNote>,
    val subContexts: List<UnifiedSubContext>
)

data class UnifiedSubContext(
    val subContext: BeraluSubContext,
    val notes: List<BeraluNote>
)

sealed interface MainScreenUiState {
    object Loading : MainScreenUiState
    data class Error(val throwable: Throwable) : MainScreenUiState
    data class Success(val data: List<UnifiedContext>) : MainScreenUiState
}
