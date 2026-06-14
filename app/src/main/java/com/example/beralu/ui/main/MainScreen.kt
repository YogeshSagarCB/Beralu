package com.example.beralu.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = hiltViewModel(),
    contextViewModel: ContextManagerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val contexts by contextViewModel.contexts.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showContextDialog by remember { mutableStateOf(false) }
    var showNoContextWarning by remember { mutableStateOf(false) }

    // Read overlay permission state
    var isBubbleRunning by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Beralu", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isBubbleRunning) "Bubble: Active" else "Bubble: Off",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Switch(
                            checked = isBubbleRunning,
                            onCheckedChange = { checked ->
                                if (!Settings.canDrawOverlays(context)) {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                } else {
                                    val intent = Intent(context, FloatingBubbleService::class.java)
                                    if (checked) {
                                        context.startService(intent)
                                        isBubbleRunning = true
                                    } else {
                                        context.stopService(intent)
                                        isBubbleRunning = false
                                    }
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        if (contexts.isEmpty()) {
                            showNoContextWarning = true
                        } else {
                            showContextDialog = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Note")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Notes") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Contexts") }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (selectedTab == 0) {
                    when (val uiState = state) {
                        is MainScreenUiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is MainScreenUiState.Success -> {
                            if (uiState.data.isEmpty()) {
                                EmptyNotesState(
                                    onCreateContextClick = { selectedTab = 1 }
                                )
                            } else {
                                NoteList(
                                    notes = uiState.data,
                                    modifier = Modifier.fillMaxSize().padding(16.dp)
                                )
                            }
                        }
                        is MainScreenUiState.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Error: ${uiState.throwable.message}", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                } else {
                    ContextManagerScreen(viewModel = contextViewModel)
                }
            }
        }
    }

    // Dialog for warning user to create a Context first
    if (showNoContextWarning) {
        AlertDialog(
            onDismissRequest = { showNoContextWarning = false },
            title = { Text("No Context Found") },
            text = { Text("Beralu associates notes with contexts (like apps or sites). Please define at least one context before writing notes.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNoContextWarning = false
                        selectedTab = 1
                    }
                ) {
                    Text("Go to Contexts")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoContextWarning = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog for picking a context before writing a Note
    if (showContextDialog) {
        AlertDialog(
            onDismissRequest = { showContextDialog = false },
            title = { Text("Select Note Context") },
            text = {
                Column {
                    Text("Choose the context to associate this note with:", modifier = Modifier.padding(bottom = 16.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(contexts) { beraluContext ->
                            Card(
                                onClick = {
                                    showContextDialog = false
                                    onItemClick(EditNote(contextId = beraluContext.id))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = beraluContext.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (!beraluContext.packageName.isNullOrBlank()) {
                                        Text(
                                            text = beraluContext.packageName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun EmptyNotesState(onCreateContextClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to Beralu!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Keep your notes handy contextually. To start, make sure you have defined a context (like an app package), then write a note linked to it.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Button(onClick = onCreateContextClick) {
                Text("Manage Contexts")
            }
        }
    }
}

@Composable
fun NoteList(notes: List<BeraluNote>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notes) { note ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = note.content,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
