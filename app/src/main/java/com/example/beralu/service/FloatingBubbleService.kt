package com.example.beralu.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import android.app.usage.UsageStatsManager
import android.app.usage.UsageStats
import android.content.pm.PackageManager
import javax.inject.Inject
import com.example.beralu.ui.context.ContextManagerViewModel
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class FloatingBubbleService : LifecycleService(), SavedStateRegistryOwner, ViewModelStoreOwner {

    private lateinit var windowManager: WindowManager
    @Inject
    lateinit var contextManagerViewModel: ContextManagerViewModel
    private lateinit var bubbleLayoutParams: WindowManager.LayoutParams

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store

    private val bubbleState = MutableStateFlow(BubbleState.COLLAPSED)

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        
        startForeground(1, createNotification())

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        bubbleLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            else 
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingBubbleService)
            setViewTreeViewModelStoreOwner(this@FloatingBubbleService)
            setViewTreeSavedStateRegistryOwner(this@FloatingBubbleService)
            setContent {
                val state by bubbleState.collectAsState()
                BubbleOverlay(
                    state = state,
                    onStateChange = { newState ->
                        bubbleState.value = newState
                        updateLayoutParams(newState)
                    },
                    onDrag = { dx, dy ->
                        bubbleLayoutParams.x += dx.toInt()
                        bubbleLayoutParams.y += dy.toInt()
                        windowManager.updateViewLayout(composeView, bubbleLayoutParams)
                    },
                    onAddContext = {
                        // Retrieve the current foreground app package name
                        val pkg = getForegroundPackageName()
                        if (pkg != null) {
                            // Use package name as both name and package identifier for simplicity
                            contextManagerViewModel.addContext(name = pkg, packageName = pkg)
                        }
                    }
                )
            }
        }

        windowManager.addView(composeView, bubbleLayoutParams)
    }

    private fun updateLayoutParams(state: BubbleState) {
        if (state == BubbleState.EXPANDED) {
            bubbleLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            bubbleLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            bubbleLayoutParams.flags = bubbleLayoutParams.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        } else {
            bubbleLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            bubbleLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            bubbleLayoutParams.flags = bubbleLayoutParams.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }
        windowManager.updateViewLayout(composeView, bubbleLayoutParams)
    }

    private fun createNotification(): Notification {
        val channelId = "floating_bubble_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Floating Bubble", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Beralu")
            .setContentText("Bubble is active")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .build()
    }

    // Helper to get the foreground app package name using UsageStatsManager
    private fun getForegroundPackageName(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 * 5 // look back 5 minutes
        val usageStatsList: List<UsageStats> = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        if (usageStatsList == null || usageStatsList.isEmpty()) return null
        val recentStat = usageStatsList.maxByOrNull { it.lastTimeUsed }
        return recentStat?.packageName
    }
        super.onDestroy()
        if (::composeView.isInitialized) {
            windowManager.removeView(composeView)
        }
        store.clear()
    }
}
