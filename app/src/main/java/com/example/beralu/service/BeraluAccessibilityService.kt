package com.example.beralu.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BeraluAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "BeraluAccessibility"
        
        private val _currentSubContext = MutableStateFlow<String?>(null)
        val currentSubContext = _currentSubContext.asStateFlow()
        
        private val _currentPackage = MutableStateFlow<String?>(null)
        val currentPackage = _currentPackage.asStateFlow()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        
        // Ignore Beralu itself and Gboard so we don't lose the context of the app underneath the bubble
        if (packageName == "com.example.beralu" || packageName == "com.google.android.inputmethod.latin") return
        
        if (packageName != _currentPackage.value) {
            _currentPackage.value = packageName
            _currentSubContext.value = null // Clear restaurant name when app changes
            Log.d(TAG, "Package changed to: $packageName")
        }

        if (packageName.startsWith("com.application.zomato")) {
            extractZomatoRestaurant(event)
        }
    }
private fun extractZomatoRestaurant(event: AccessibilityEvent) {
    val rootNode = rootInActiveWindow ?: return

    // Zomato stores restaurant name in content-desc, often on a node 
    // that may not have a static ID. Search the tree.
    val restaurantName = findRestaurantName(rootNode)

    if (restaurantName != null) {
        _currentSubContext.value = restaurantName
        Log.d(TAG, "Detected restaurant successfully: $restaurantName")
    } else {
        Log.d(TAG, "Failed to find restaurant name using traversal heuristic")
    }
}

private fun findRestaurantName(node: AccessibilityNodeInfo): String? {
    val contentDesc = node.contentDescription?.toString()
    // Heuristic: Restaurant names on Zomato have a specific icon appended
    if (!contentDesc.isNullOrBlank() && contentDesc.contains("")) {
        return contentDesc.replace("", "").trim()
    }

    for (i in 0 until node.childCount) {
        val child = node.getChild(i)
        if (child != null) {
            val found = findRestaurantName(child)
            if (found != null) return found
        }
    }
    return null
}

    private fun searchForRestaurantName(node: AccessibilityNodeInfo) {
        if (node.text != null) {
             Log.d(TAG, "Traversing node, text: ${node.text}, class: ${node.className}")
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                searchForRestaurantName(child)
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility service connected")
    }
}
