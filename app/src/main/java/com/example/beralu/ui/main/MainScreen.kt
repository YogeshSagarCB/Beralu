package com.example.beralu.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.beralu.theme.*
import androidx.navigation3.runtime.NavKey
import com.example.beralu.EditNote
import com.example.beralu.domain.model.BeraluNote
import com.example.beralu.service.FloatingBubbleService
import com.example.beralu.ui.components.GlassCard
import com.example.beralu.ui.context.ContextManagerScreen
import com.example.beralu.ui.context.ContextManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Beralu Dashboard", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val uiState = state) {
                is MainScreenUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is MainScreenUiState.Success -> {
                    if (uiState.data.isEmpty()) {
                        EmptyDashboardState()
                    } else {
                        UnifiedDashboard(
                            data = uiState.data,
                            viewModel = viewModel
                        )
                    }
                }
                is MainScreenUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${uiState.throwable.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedDashboard(
    data: List<UnifiedContext>,
    viewModel: MainScreenViewModel
) {
    val context = LocalContext.current
    val packageManager = remember { context.packageManager }
    val appNameCache = remember { mutableMapOf<String, String>() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        data.forEach { unifiedCtx ->
            item(key = unifiedCtx.context.id) {
                val pkgName = unifiedCtx.context.packageName
                val appName = if (pkgName != null) {
                    if (pkgName.startsWith("com.application.zomato")) {
                        "Zomato"
                    } else {
                        appNameCache.getOrPut(pkgName) {
                            try {
                                val info = packageManager.getApplicationInfo(pkgName, 0)
                                packageManager.getApplicationLabel(info).toString()
                            } catch (e: Exception) {
                                pkgName
                            }
                        }
                    }
                } else unifiedCtx.context.name

                var showMenu by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = appName, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, "Actions")
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Delete App Context", color = LightActionDelete) },
                                    onClick = { 
                                        viewModel.deleteContext(unifiedCtx.context.id)
                                        showMenu = false 
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear All Notes", color = LightActionDelete) },
                                    onClick = { 
                                        viewModel.deleteNotesByContext(unifiedCtx.context.id)
                                        showMenu = false 
                                    }
                                )
                            }
                        }

                        // Notes & Subcontexts
                        unifiedCtx.notes.forEach { note ->
                            NoteItem(note = note, onDelete = { viewModel.deleteNote(note.id) })
                        }
                        
                        unifiedCtx.subContexts.forEach { sub ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
                                Text(text = sub.subContext.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.deleteSubContext(sub.subContext.id) }) {
                                    Icon(Icons.Default.Delete, "Delete Subcontext", tint = LightActionDelete)
                                }
                            }
                            sub.notes.forEach { note ->
                                NoteItem(note = note, onDelete = { viewModel.deleteNote(note.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteItem(note: BeraluNote, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) LightActionDelete else Color.Transparent
            Box(Modifier.fillMaxSize().background(color).padding(16.dp), contentAlignment = Alignment.CenterEnd) {
                Icon(Icons.Default.Delete, "Delete", tint = Color.White)
            }
        },
        content = {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = note.content, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Delete", tint = LightActionDelete)
                    }
                }
            }
        }
    )
}

@Composable
fun EmptyDashboardState() {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = "No notes or contexts yet. Start using the bubble in other apps to capture notes contextually!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
