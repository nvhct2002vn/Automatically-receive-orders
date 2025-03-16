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
        val rootNode = rootInActiveWindow // Lấy root node của màn hình
        if (rootNode != null) {
            logAllTexts(rootNode) // Gọi hàm để quét và in văn bản
        }
//        --------------------------------------------------------------------------------------
        // Lấy package name của sự kiện
        val eventPackageName = event?.packageName?.toString()
        val appPackageName = packageName // packageName là package name của ứng dụng chính

        // Kiểm tra nếu sự kiện đến từ ứng dụng chính
        if (eventPackageName == appPackageName) {
            Log.d(TAG, "Sự kiện từ ứng dụng chính, không thực hiện hành động")
            return // Thoát khỏi hàm, không thực hiện bất kỳ hành động nào
        }

        // Lấy các giá trị từ SharedPreferences
        val autoSelectEnabled = sharedPreferences.getBoolean("auto_select_enabled", false)
        val criterion = sharedPreferences.getString("match_criterion", "")?.trim()

        // Tiếp tục xử lý nếu autoSelectEnabled bật và criterion không rỗng
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

    // Hàm đệ quy để duyệt qua tất cả các node và in văn bản
    private fun logAllTexts(node: AccessibilityNodeInfo) {
        // Kiểm tra và in văn bản của node hiện tại
        val text = node.text
        if (!text.isNullOrEmpty()) {
            Log.d(TAG, "Text: $text")
        }

        // Duyệt qua tất cả các node con
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                logAllTexts(child) // Đệ quy cho node con
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Dịch vụ bị gián đoạn")
    }
}