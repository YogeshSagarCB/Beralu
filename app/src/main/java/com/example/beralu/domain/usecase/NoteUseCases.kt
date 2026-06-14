package com.example.beralu.domain.usecase

import com.example.beralu.domain.model.BeraluNote
import com.example.beralu.domain.repository.BeraluRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotesForActiveContextUseCase @Inject constructor(
    private val repository: BeraluRepository
) {
    operator fun invoke(contextId: String): Flow<List<BeraluNote>> {
        return repository.getNotesByContext(contextId)
    }
}

class SaveNoteUseCase @Inject constructor(
    private val repository: BeraluRepository
) {
    suspend operator fun invoke(note: BeraluNote) {
        // If it's a new note or updating an existing one, Room handles it via UPSERT (REPLACE)
        repository.insertNote(note)
    }
}
