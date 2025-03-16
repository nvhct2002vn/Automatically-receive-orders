package com.example.automatically_receive_orders

import android.accessibilityservice.AccessibilityService
import android.content.SharedPreferences
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {
    private val TAG = "MyAccessibilityService"
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("AutoAcceptPrefs", MODE_PRIVATE)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val autoSelectEnabled = sharedPreferences.getBoolean("auto_select_enabled", false)
        val criterion = sharedPreferences.getString("match_criterion", "")?.trim()

        if (autoSelectEnabled && !criterion.isNullOrEmpty()) {
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                val clicked = findAndClickNode(rootNode, criterion)
                if (clicked) {
                    Log.d(TAG, "Đã nhấp vào phần tử khớp với tiêu chí: $criterion")
                } else {
                    Log.d(TAG, "Không tìm thấy phần tử khớp với tiêu chí: $criterion")
                }
            }
        } else {
            Log.d(TAG, "Chưa bật tự động chọn đơn hoặc tiêu chí rỗng")
        }
    }

    private fun findAndClickNode(node: AccessibilityNodeInfo, criterion: String): Boolean {
        val text = node.text?.toString()?.lowercase()
        if (text != null && text.contains(criterion.lowercase())) {
            if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "Đã nhấp vào phần tử: $text")
                return true
            } else {
                var parent = node.parent
                while (parent != null) {
                    if (parent.isClickable) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Log.d(TAG, "Đã nhấp vào phần tử cha: ${parent.text}")
                        return true
                    }
                    parent = parent.parent
                }
            }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                if (findAndClickNode(child, criterion)) {
                    return true
                }
            }
        }
        return false
    }

    override fun onInterrupt() {
        Log.d(TAG, "Dịch vụ bị gián đoạn")
    }
}