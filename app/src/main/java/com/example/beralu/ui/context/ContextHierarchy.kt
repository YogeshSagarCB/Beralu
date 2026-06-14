package com.example.beralu.ui.context

import com.example.beralu.domain.model.BeraluContext
import com.example.beralu.domain.model.BeraluNote
import com.example.beralu.domain.model.BeraluSubContext

data class ContextHierarchy(
    val context: BeraluContext,
    val subContexts: List<SubContextHierarchy>,
    val notes: List<BeraluNote>
)

data class SubContextHierarchy(
    val subContext: BeraluSubContext,
    val notes: List<BeraluNote>
)
