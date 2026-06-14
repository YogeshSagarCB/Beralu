package com.example.beralu.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "contexts")
data class ContextEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val packageName: String?,
    val colorHex: String,
    val createdAt: Long = System.currentTimeMillis()
)
