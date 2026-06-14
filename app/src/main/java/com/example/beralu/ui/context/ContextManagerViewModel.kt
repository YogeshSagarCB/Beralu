package com.example.beralu.ui.context

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beralu.domain.model.BeraluContext
import com.example.beralu.domain.repository.BeraluRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContextManagerViewModel @Inject constructor(
    private val repository: BeraluRepository
) : ViewModel() {

    val contexts: StateFlow<List<BeraluContext>> = repository.getContexts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addContext(name: String, packageName: String) {
        viewModelScope.launch {
            repository.insertContext(
                BeraluContext(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    packageName = packageName,
                    colorHex = "#6C63FF", // Default color
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteContext(contextId: String) {
        viewModelScope.launch {
            repository.deleteContext(contextId)
        }
    }
}
