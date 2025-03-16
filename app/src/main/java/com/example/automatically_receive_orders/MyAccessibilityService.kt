package com.example.automatically_receive_orders

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {
    private val TAG = "MyAccessibilityService"

    // Hàm này được gọi khi có sự kiện trên màn hình
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootNode = rootInActiveWindow // Lấy root node của màn hình
        if (rootNode != null) {
            logAllTexts(rootNode) // Gọi hàm để quét và in văn bản
        }
    }

    // Hàm đệ quy để duyệt qua tất cả các node và in văn bản
    private fun logAllTexts(node: AccessibilityNodeInfo) {
        // Kiểm tra và in văn bản của node hiện tại
        val text = node.text
        if (!text.isNullOrEmpty()) {
            Log.d(TAG, "Text quét được trên màn hình: $text")
        }

        // Duyệt qua tất cả các node con
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                logAllTexts(child) // Đệ quy cho node con
            }
        }
    }

    // Hàm này được gọi khi dịch vụ bị gián đoạn
    override fun onInterrupt() {
        Log.d(TAG, "Dịch vụ bị gián đoạn")
    }
}