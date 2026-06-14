package com.example.beralu.service

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun BubbleOverlay(
    state: BubbleState,
    onStateChange: (BubbleState) -> Unit,
    onDrag: (Float, Float) -> Unit,
    onAddContext: () -> Unit
) {
    if (state == BubbleState.COLLAPSED) {
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
                .clickable { onStateChange(BubbleState.EXPANDED) }
        ) {
            Text("B", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onStateChange(BubbleState.COLLAPSED) },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
                    .clickable(enabled = false) {}, // Prevent clicks from closing overlay
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Expanded Panel", color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onStateChange(BubbleState.COLLAPSED) }) {
                        Text("Close")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onAddContext) {
                        Text("Add Context")
                    }
                }
            }
        }
    }
}
