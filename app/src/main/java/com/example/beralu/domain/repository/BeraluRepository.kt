package com.example.beralu.domain.repository

import com.example.beralu.domain.model.BeraluContext
import com.example.beralu.domain.model.BeraluNote
import kotlinx.coroutines.flow.Flow

interface BeraluRepository {
    fun getContexts(): Flow<List<BeraluContext>>
    fun getAllNotes(): Flow<List<BeraluNote>>
    fun getNotesByContext(contextId: String): Flow<List<BeraluNote>>
    suspend fun getContextById(contextId: String): BeraluContext?
    suspend fun insertContext(context: BeraluContext)
    suspend fun insertNote(note: BeraluNote)
    suspend fun updateNote(note: BeraluNote)
    suspend fun deleteContext(contextId: String)
    suspend fun deleteNote(noteId: String)
}
