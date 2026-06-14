package com.example.beralu.data.repository

import com.example.beralu.data.db.dao.BeraluDao
import com.example.beralu.data.db.entities.ContextEntity
import com.example.beralu.data.db.entities.NoteEntity
import com.example.beralu.data.db.entities.SubContextEntity
import com.example.beralu.domain.model.BeraluContext
import com.example.beralu.domain.model.BeraluNote
import com.example.beralu.domain.model.BeraluSubContext
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

    override fun getNotesByContext(contextId: String, subContextId: String?): Flow<List<BeraluNote>> {
        return dao.getNotesByContext(contextId, subContextId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getNotesByPackage(packageName: String, subContextId: String?): Flow<List<BeraluNote>> {
        return dao.getNotesByPackage(packageName, subContextId).map { list ->
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

    override suspend fun deleteNotesByContext(contextId: String) {
        dao.deleteNotesByContext(contextId)
    }

    override suspend fun deleteNote(noteId: String) {
        dao.deleteNote(noteId)
    }

    override fun getSubContexts(contextId: String): Flow<List<BeraluSubContext>> {
        return dao.getSubContexts(contextId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllSubContexts(): Flow<List<BeraluSubContext>> {
        return dao.getAllSubContexts().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertSubContext(subContext: BeraluSubContext) {
        dao.insertSubContext(subContext.toEntity())
    }

    override suspend fun deleteSubContext(subContextId: String) {
        dao.deleteSubContext(subContextId)
    }

    override suspend fun getContextByPackage(packageName: String): BeraluContext? {
        return dao.getContextByPackage(packageName)?.toDomain()
    }

    override suspend fun getSubContextByName(contextId: String, name: String): BeraluSubContext? {
        return dao.getSubContextByName(contextId, name)?.toDomain()
    }
}

// Extension functions for mapping
fun ContextEntity.toDomain() = BeraluContext(id, name, packageName, colorHex, createdAt)
fun BeraluContext.toEntity() = ContextEntity(id, name, packageName, colorHex, createdAt)

fun SubContextEntity.toDomain() = BeraluSubContext(id, contextId, name, createdAt)
fun BeraluSubContext.toEntity() = SubContextEntity(id, contextId, name, createdAt)

fun NoteEntity.toDomain() = BeraluNote(id, contextId, subContextId, content, isRichText, createdAt, updatedAt)
fun BeraluNote.toEntity() = NoteEntity(id, contextId, subContextId, content, isRichText, createdAt, updatedAt)
