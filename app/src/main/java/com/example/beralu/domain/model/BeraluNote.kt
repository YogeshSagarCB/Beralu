package com.example.beralu.domain.model

data class BeraluNote(
    val id: String,
    val contextId: String,
    val subContextId: String?,
    val content: String,
    val isRichText: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
