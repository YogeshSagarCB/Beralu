package com.example.beralu.domain.repository

import com.example.beralu.domain.model.BeraluContext
import com.example.beralu.domain.model.BeraluNote
import com.example.beralu.domain.model.BeraluSubContext
import kotlinx.coroutines.flow.Flow

interface BeraluRepository {
    fun getContexts(): Flow<List<BeraluContext>>
    fun getAllNotes(): Flow<List<BeraluNote>>
    fun getNotesByContext(contextId: String, subContextId: String? = null): Flow<List<BeraluNote>>
    fun getNotesByPackage(packageName: String, subContextId: String? = null): Flow<List<BeraluNote>>
    suspend fun getContextById(contextId: String): BeraluContext?
    suspend fun insertContext(context: BeraluContext)
    suspend fun insertNote(note: BeraluNote)
    suspend fun updateNote(note: BeraluNote)
    suspend fun deleteContext(contextId: String)
    suspend fun deleteNotesByContext(contextId: String)
    suspend fun deleteNote(noteId: String)

    fun getSubContexts(contextId: String): Flow<List<BeraluSubContext>>
    fun getAllSubContexts(): Flow<List<BeraluSubContext>>
    suspend fun insertSubContext(subContext: BeraluSubContext)
    suspend fun deleteSubContext(subContextId: String)

    suspend fun getContextByPackage(packageName: String): BeraluContext?
    suspend fun getSubContextByName(contextId: String, name: String): BeraluSubContext?
}
