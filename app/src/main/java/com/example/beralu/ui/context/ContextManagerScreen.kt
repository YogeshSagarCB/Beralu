package com.example.beralu.ui.context

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.beralu.ui.components.GlassCard

@Composable
fun ContextManagerScreen(
    viewModel: ContextManagerViewModel = hiltViewModel()
) {
    val hierarchy by viewModel.contextHierarchy.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var expandedContexts by remember { mutableStateOf(setOf<String>()) }
    var expandedSubContexts by remember { mutableStateOf(setOf<String>()) }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var pkg by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Context") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                    OutlinedTextField(value = pkg, onValueChange = { pkg = it }, label = { Text("Package") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addContext(name, pkg)
                    showAddDialog = false
                }) { Text("Add") }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add Context")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(hierarchy) { item ->
                val isExpanded = expandedContexts.contains(item.context.id)
                GlassCard(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Column {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                expandedContexts = if (isExpanded) expandedContexts - item.context.id else expandedContexts + item.context.id
                            }) {
                                Icon(if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, "Toggle")
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.context.name, style = MaterialTheme.typography.titleMedium)
                                Text(text = item.context.packageName ?: "", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.deleteContext(item.context.id) }) {
                                Icon(Icons.Default.Delete, "Delete")
                            }
                        }
                        if (isExpanded) {
                            // Render Notes in Context
                            item.notes.forEach { note ->
                                Row(modifier = Modifier.padding(start = 32.dp, end = 16.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = note.content, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { viewModel.deleteNote(note.id) }) {
                                        Icon(Icons.Default.Delete, "Delete")
                                    }
                                }
                            }
                            // Render Sub-Contexts
                            item.subContexts.forEach { subItem ->
                                val isSubExpanded = expandedSubContexts.contains(subItem.subContext.id)
                                Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        expandedSubContexts = if (isSubExpanded) expandedSubContexts - subItem.subContext.id else expandedSubContexts + subItem.subContext.id
                                    }) {
                                        Icon(if (isSubExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, "Toggle")
                                    }
                                    Text(text = subItem.subContext.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { viewModel.deleteSubContext(subItem.subContext.id) }) {
                                        Icon(Icons.Default.Delete, "Delete")
                                    }
                                }
                                if (isSubExpanded) {
                                    subItem.notes.forEach { note ->
                                        Row(modifier = Modifier.padding(start = 64.dp, end = 16.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = note.content, modifier = Modifier.weight(1f))
                                            IconButton(onClick = { viewModel.deleteNote(note.id) }) {
                                                Icon(Icons.Default.Delete, "Delete")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
