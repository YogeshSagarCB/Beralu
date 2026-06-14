package com.example.beralu.service

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beralu.domain.model.BeraluNote
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BubbleOverlay(
    state: BubbleState,
    notes: List<BeraluNote>,
    currentContextName: String?,
    hasUsagePermission: Boolean,
    onStateChange: (BubbleState) -> Unit,
    onDrag: (Float, Float) -> Unit,
    onAddNote: (String) -> Unit,
    onRequestPermission: () -> Unit
) {
    if (state == BubbleState.COLLAPSED) {
        val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF6C63FF))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onStateChange(BubbleState.EXPANDED) }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("B", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                if (notes.isNotEmpty()) {
                    Badge(
                        modifier = Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp),
                        containerColor = Color(0xFFFF4081)
                    ) {
                        Text(notes.size.toString(), fontSize = 10.sp)
                    }
                }
            }
        }
    } else {
        // Full screen container for the expanded state
        val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent) // The whole overlay is now transparent
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onStateChange(BubbleState.COLLAPSED) }, // Click outside to close
            contentAlignment = Alignment.Center
        ) {
            // Panel — with its own background
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { /* consume click inside the card, don't collapse */ },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E).copy(alpha = 0.9f))
            ) {
                PanelContent(
                    notes = notes,
                    currentContextName = currentContextName,
                    hasUsagePermission = hasUsagePermission,
                    onAddNote = onAddNote,
                    onRequestPermission = onRequestPermission
                )
            }
        }
    }
}

@Composable
private fun PanelContent(
    notes: List<BeraluNote>,
    currentContextName: String?,
    hasUsagePermission: Boolean,
    onAddNote: (String) -> Unit,
    onRequestPermission: () -> Unit
) {
    var showNoteInput by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Beralu",
                    color = Color(0xFF6C63FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = currentContextName ?: "No context detected",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
            if (!showNoteInput) {
                IconButton(
                    onClick = {
                        if (!hasUsagePermission) {
                            onRequestPermission()
                        } else {
                            showNoteInput = true
                        }
                    },
                    modifier = Modifier.size(48.dp) // Ensure explicit touch target size
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Note",
                        tint = Color(0xFF6C63FF),
                        modifier = Modifier.size(32.dp) // Explicit icon size
                    )
                }
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

        // Note input form
        if (showNoteInput) {
            Column {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("Write a note...", color = Color.White.copy(alpha = 0.4f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6C63FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = Color(0xFF6C63FF)
                    ),
                    maxLines = 5,
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            showNoteInput = false
                            noteText = ""
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.6f))
                    ) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (noteText.isNotBlank()) {
                                Log.d("BubbleOverlay", "Saving note: $noteText")
                                onAddNote(noteText.trim())
                                noteText = ""
                                showNoteInput = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                    ) { Text("Save") }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        // Notes list
        if (notes.isEmpty()) {
            val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (!hasUsagePermission) {
                            Modifier.clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onRequestPermission() }
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (!hasUsagePermission)
                        "Grant Usage Access to detect\nthe current app context"
                    else
                        "No notes for this context yet.\nTap + to add one.",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notes) { note ->
                    NoteCard(note = note)
                }
            }
        }
    }
}

@Composable
private fun NoteCard(note: BeraluNote) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val timestamp = remember(note.createdAt) { dateFormat.format(Date(note.createdAt)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = note.content,
                color = Color.White,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timestamp,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
