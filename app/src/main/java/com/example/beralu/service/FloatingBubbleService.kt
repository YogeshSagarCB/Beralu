package com.example.beralu.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import android.app.usage.UsageStatsManager
import android.app.usage.UsageStats
import javax.inject.Inject
import com.example.beralu.domain.repository.BeraluRepository
import com.example.beralu.domain.model.BeraluContext
import com.example.beralu.domain.model.BeraluNote
import com.example.beralu.domain.model.BeraluSubContext
import kotlinx.coroutines.launch
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class FloatingBubbleService : Service(), SavedStateRegistryOwner, ViewModelStoreOwner, LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView
    private lateinit var bubbleLayoutParams: WindowManager.LayoutParams

    @Inject
    lateinit var repository: BeraluRepository

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store

    private val bubbleState = MutableStateFlow(BubbleState.COLLAPSED)
    private val hasUsagePermission = MutableStateFlow(false)
    private val foregroundPackagePoll = MutableStateFlow<String?>(null)

    private var lastX = 0
    private var lastY = 200

    private lateinit var currentContextFlow: StateFlow<BeraluContext?>
    private lateinit var currentSubContextFlow: StateFlow<BeraluSubContext?>
    private lateinit var notesFlow: StateFlow<List<BeraluNote>>

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        startForeground(1, createNotification())

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        lifecycleScope.launch {
            while (true) {
                foregroundPackagePoll.value = getForegroundPackageName()
                kotlinx.coroutines.delay(2000)
            }
        }

        bubbleLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = lastX
            y = lastY
        }

        hasUsagePermission.value = checkUsagePermission()
        
        currentContextFlow = combine(
            repository.getContexts(),
            BeraluAccessibilityService.currentPackage,
            foregroundPackagePoll
        ) { contexts, accessibilityPackage, polledPackage ->
            val pkg = accessibilityPackage ?: polledPackage
            contexts.find { it.packageName == pkg }
        }.stateIn(lifecycleScope, SharingStarted.Eagerly, null)

        currentSubContextFlow = combine(
            currentContextFlow,
            BeraluAccessibilityService.currentSubContext
        ) { context, subContextName ->
            context to subContextName
        }.flatMapLatest { (context, subContextName) ->
            if (context != null && subContextName != null) {
                repository.getSubContexts(context.id).map { subContexts ->
                    subContexts.find { it.name == subContextName }
                }
            } else {
                flowOf(null)
            }
        }.stateIn(lifecycleScope, SharingStarted.Eagerly, null)

        notesFlow = combine(
            BeraluAccessibilityService.currentPackage,
            foregroundPackagePoll,
            currentSubContextFlow
        ) { accessibilityPackage, polledPackage, subContext ->
            Triple(accessibilityPackage, polledPackage, subContext)
        }.flatMapLatest { (accessibilityPackage, polledPackage, subContext) ->
            val pkg = accessibilityPackage ?: polledPackage
            if (pkg != null) {
                repository.getNotesByPackage(pkg, subContext?.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(lifecycleScope, SharingStarted.Eagerly, emptyList())

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingBubbleService)
            setViewTreeViewModelStoreOwner(this@FloatingBubbleService)
            setViewTreeSavedStateRegistryOwner(this@FloatingBubbleService)

            // Handle back button manually to collapse bubble
            isFocusableInTouchMode = true
            requestFocus()
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    if (bubbleState.value == BubbleState.EXPANDED) {
                        Log.d("FloatingBubbleService", "Back key detected: collapsing bubble")
                        bubbleState.value = BubbleState.COLLAPSED
                        updateLayoutParams(BubbleState.COLLAPSED)
                        return@setOnKeyListener true
                    }
                }
                false
            }

            setContent {
                val state by bubbleState.collectAsState()
                val notes by notesFlow.collectAsState()
                val currentContext by currentContextFlow.collectAsState()
                val currentSubContext by currentSubContextFlow.collectAsState()
                val usagePermission by hasUsagePermission.collectAsState()

                // State to drive the theme inside the overlay
                // Note: In a real app, this should be persisted in DataStore
                val isDarkTheme by remember { mutableStateOf(false) } // Placeholder until we persist the theme

                com.example.beralu.theme.BeraluTheme(darkTheme = isDarkTheme) {
                    BubbleOverlay(
                        state = state,
                        notes = notes,
                        currentContextName = if (currentSubContext != null) 
                            "${currentContext?.name} > ${currentSubContext?.name}" 
                        else 
                            currentContext?.name,
                        hasUsagePermission = usagePermission,
                        onStateChange = { newState ->
                            if (newState == BubbleState.EXPANDED) {
                                hasUsagePermission.value = checkUsagePermission()
                                requestFocus() // Re-focus to capture back key
                            }
                            bubbleState.value = newState
                            updateLayoutParams(newState)
                        },
                        onDrag = { dx, dy ->
                            if (bubbleState.value == BubbleState.COLLAPSED) {
                                bubbleLayoutParams.x += dx.toInt()
                                bubbleLayoutParams.y += dy.toInt()
                                lastX = bubbleLayoutParams.x
                                lastY = bubbleLayoutParams.y
                                windowManager.updateViewLayout(composeView, bubbleLayoutParams)
                            }
                        },
                        onAddNote = { noteContent ->
                            lifecycleScope.launch {
                                Log.d("FloatingBubbleService", "onAddNote: $noteContent")
                                val pkg = BeraluAccessibilityService.currentPackage.value ?: getForegroundPackageName()
                                val subContextName = BeraluAccessibilityService.currentSubContext.value
                                
                                val ctx = if (pkg != null) repository.getContextByPackage(pkg) ?: BeraluContext(
                                            id = java.util.UUID.randomUUID().toString(),
                                            name = pkg,
                                            packageName = pkg,
                                            colorHex = "#6C63FF",
                                            createdAt = System.currentTimeMillis()
                                        ).also { repository.insertContext(it) } else null

                                if (ctx != null) {
                                    val subCtx = if (subContextName != null) {
                                        repository.getSubContextByName(ctx.id, subContextName) ?: BeraluSubContext(
                                            id = java.util.UUID.randomUUID().toString(),
                                            contextId = ctx.id,
                                            name = subContextName,
                                            createdAt = System.currentTimeMillis()
                                        ).also { repository.insertSubContext(it) }
                                    } else null
                                    
                                    repository.insertNote(BeraluNote(
                                        id = java.util.UUID.randomUUID().toString(),
                                        contextId = ctx.id,
                                        subContextId = subCtx?.id,
                                        content = noteContent,
                                        isRichText = false,
                                        createdAt = System.currentTimeMillis(),
                                        updatedAt = System.currentTimeMillis()
                                    ))
                                    Log.d("FloatingBubbleService", "Saved note in SubContext: ${subCtx?.name}")
                                    composeView.requestFocus()
                                }
                            }
                        },
                        onRequestPermission = {
                            val intent = Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            startActivity(intent)
                        }
                    )
                }
            }
        }

        windowManager.addView(composeView, bubbleLayoutParams)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    private fun checkUsagePermission(): Boolean = (getSystemService(APP_OPS_SERVICE) as android.app.AppOpsManager)
        .checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), packageName) == android.app.AppOpsManager.MODE_ALLOWED

    private fun updateLayoutParams(state: BubbleState) {
        if (state == BubbleState.EXPANDED) {
            bubbleLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            bubbleLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            bubbleLayoutParams.x = 0; bubbleLayoutParams.y = 0
            bubbleLayoutParams.flags = bubbleLayoutParams.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv() or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        } else {
            bubbleLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            bubbleLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            bubbleLayoutParams.x = lastX; bubbleLayoutParams.y = lastY
            bubbleLayoutParams.flags = bubbleLayoutParams.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE and WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL.inv()
        }
        windowManager.updateViewLayout(composeView, bubbleLayoutParams)
    }

    private fun createNotification(): Notification = NotificationCompat.Builder(this, "floating_bubble_channel")
            .setContentTitle("Beralu").setContentText("Bubble is active").setSmallIcon(android.R.drawable.ic_menu_edit).build()
            .also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("floating_bubble_channel", "Floating Bubble", NotificationManager.IMPORTANCE_LOW))
                }
            }

    private fun getForegroundPackageName(): String? = (getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager)
        .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, System.currentTimeMillis() - 1000 * 60 * 5, System.currentTimeMillis())
        .maxByOrNull { it.lastTimeUsed }?.packageName

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        if (::composeView.isInitialized) windowManager.removeView(composeView)
        store.clear()
        super.onDestroy()
    }
}
