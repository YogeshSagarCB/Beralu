package com.example.beralu.domain.model

data class BeraluContext(
    val id: String,
    val name: String,
    val packageName: String?,
    val colorHex: String,
    val createdAt: Long
)
