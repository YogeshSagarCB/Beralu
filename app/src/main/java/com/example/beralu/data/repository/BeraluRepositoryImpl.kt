package com.example.beralu.data.repository

import com.example.beralu.data.db.dao.BeraluDao
import com.example.beralu.data.db.entities.ContextEntity
import com.example.beralu.data.db.entities.NoteEntity
import com.example.beralu.domain.model.BeraluContext
import com.example.beralu.domain.model.BeraluNote
import com.example.beralu.domain.repository.BeraluRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BeraluRepositoryImpl @Inject constructor(
    private val dao: BeraluDao
) : BeraluRepository {

    override fun getContexts(): Flow<List<BeraluContext>> {
        return dao.getContexts().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllNotes(): Flow<List<BeraluNote>> {
        return dao.getAllNotes().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getNotesByContext(contextId: String): Flow<List<BeraluNote>> {
        return dao.getNotesByContext(contextId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getContextById(contextId: String): BeraluContext? {
        return dao.getContextById(contextId)?.toDomain()
    }

    override suspend fun insertContext(context: BeraluContext) {
        dao.insertContext(context.toEntity())
    }

    override suspend fun insertNote(note: BeraluNote) {
        dao.insertNote(note.toEntity())
    }

    override suspend fun updateNote(note: BeraluNote) {
        dao.updateNote(note.toEntity())
    }

    override suspend fun deleteContext(contextId: String) {
        dao.deleteContext(contextId)
    }

    override suspend fun deleteNote(noteId: String) {
        dao.deleteNote(noteId)
    }
}

// Extension functions for mapping
fun ContextEntity.toDomain() = BeraluContext(id, name, packageName, colorHex, createdAt)
fun BeraluContext.toEntity() = ContextEntity(id, name, packageName, colorHex, createdAt)

fun NoteEntity.toDomain() = BeraluNote(id, contextId, subContextId, content, isRichText, createdAt, updatedAt)
fun BeraluNote.toEntity() = NoteEntity(id, contextId, subContextId, content, isRichText, createdAt, updatedAt)
