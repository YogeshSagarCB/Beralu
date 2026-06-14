package com.example.beralu.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Query("SELECT * FROM notes WHERE contextId = :contextId ORDER BY updatedAt DESC")
    fun getNotesByContext(contextId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContext(context: ContextEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubContext(subContext: SubContextEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)
    
    @Query("DELETE FROM contexts WHERE id = :contextId")
    suspend fun deleteContext(contextId: String)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: String)
}
