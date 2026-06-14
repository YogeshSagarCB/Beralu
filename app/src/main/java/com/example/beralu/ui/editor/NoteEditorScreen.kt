package com.example.beralu.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.beralu.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    contextId: String, // Assume contextId is passed for new note
    onNavigateBack: () -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel()
) {
    var content by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Note") },
                actions = {
                    TextButton(onClick = {
                        viewModel.saveNote(content, contextId)
                        onNavigateBack()
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        GlassCard(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxSize().padding(16.dp),
                placeholder = { Text("Enter your note...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}
