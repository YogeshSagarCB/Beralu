package com.example.beralu.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = ContextEntity::class,
            parentColumns = ["id"],
            childColumns = ["contextId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SubContextEntity::class,
            parentColumns = ["id"],
            childColumns = ["subContextId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["contextId"]), Index(value = ["subContextId"])]
)
data class NoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val contextId: String,
    val subContextId: String? = null,
    val content: String,
    val isRichText: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
