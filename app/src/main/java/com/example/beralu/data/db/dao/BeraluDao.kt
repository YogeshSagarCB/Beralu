package com.example.beralu.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.beralu.data.db.entities.ContextEntity
import com.example.beralu.data.db.entities.NoteEntity
import com.example.beralu.data.db.entities.SubContextEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BeraluDao {
    @Query("SELECT * FROM contexts ORDER BY createdAt DESC")
    fun getContexts(): Flow<List<ContextEntity>>

    @Query("SELECT * FROM contexts WHERE id = :contextId")
    suspend fun getContextById(contextId: String): ContextEntity?

    @Query("SELECT * FROM notes WHERE contextId = :contextId AND (:subContextId IS NULL OR subContextId = :subContextId OR subContextId IS NULL) ORDER BY updatedAt DESC")
    fun getNotesByContext(contextId: String, subContextId: String? = null): Flow<List<NoteEntity>>

    @Query("SELECT n.* FROM notes n INNER JOIN contexts c ON n.contextId = c.id WHERE c.packageName = :packageName AND (:subContextId IS NULL OR n.subContextId = :subContextId OR n.subContextId IS NULL) ORDER BY n.updatedAt DESC")
    fun getNotesByPackage(packageName: String, subContextId: String? = null): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContext(context: ContextEntity)

    @Query("SELECT * FROM sub_contexts WHERE contextId = :contextId ORDER BY createdAt DESC")
    fun getSubContexts(contextId: String): Flow<List<SubContextEntity>>

    @Query("SELECT * FROM sub_contexts ORDER BY createdAt DESC")
    fun getAllSubContexts(): Flow<List<SubContextEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubContext(subContext: SubContextEntity)

    @Query("DELETE FROM sub_contexts WHERE id = :subContextId")
    suspend fun deleteSubContextRecord(subContextId: String)
    
    @Query("DELETE FROM notes WHERE subContextId = :subContextId")
    suspend fun deleteNotesBySubContext(subContextId: String)

    @Transaction
    suspend fun deleteSubContext(subContextId: String) {
        deleteNotesBySubContext(subContextId)
        deleteSubContextRecord(subContextId)
    }

    @Query("DELETE FROM notes WHERE contextId = :contextId")
    suspend fun deleteNotesByContext(contextId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)
    
    @Query("DELETE FROM contexts WHERE id = :contextId")
    suspend fun deleteContext(contextId: String)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: String)

    @Query("SELECT * FROM contexts WHERE packageName = :packageName LIMIT 1")
    suspend fun getContextByPackage(packageName: String): ContextEntity?

    @Query("SELECT * FROM sub_contexts WHERE contextId = :contextId AND name = :name LIMIT 1")
    suspend fun getSubContextByName(contextId: String, name: String): SubContextEntity?
}
