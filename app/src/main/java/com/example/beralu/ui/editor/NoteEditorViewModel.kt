package com.example.beralu.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beralu.domain.model.BeraluNote
import com.example.beralu.domain.repository.BeraluRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val repository: BeraluRepository
) : ViewModel() {

    fun saveNote(content: String, contextId: String) {
        viewModelScope.launch {
            repository.insertNote(
                BeraluNote(
                    id = UUID.randomUUID().toString(),
                    contextId = contextId,
                    subContextId = null,
                    content = content,
                    isRichText = false,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
