package com.example.beralu.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "sub_contexts",
    foreignKeys = [
        ForeignKey(
            entity = ContextEntity::class,
            parentColumns = ["id"],
            childColumns = ["contextId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["contextId"])]
)
data class SubContextEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val contextId: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
