package com.example.beralu.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.beralu.data.db.dao.BeraluDao
import com.example.beralu.data.db.entities.ContextEntity
import com.example.beralu.data.db.entities.NoteEntity
import com.example.beralu.data.db.entities.SubContextEntity

@Database(
    entities = [ContextEntity::class, SubContextEntity::class, NoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BeraluDatabase : RoomDatabase() {
    abstract fun beraluDao(): BeraluDao
}
