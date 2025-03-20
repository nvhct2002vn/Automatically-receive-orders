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

//    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
//        val eventPackageName = event?.packageName?.toString()
//        val appPackageName = packageName
//
//        if (eventPackageName == appPackageName) {
//            Log.d(TAG, "Sự kiện từ ứng dụng chính, không thực hiện hành động")
//            return
//        }
//
//        val autoSelectEnabled = sharedPreferences.getBoolean("auto_select_enabled", false)
//        val criterion1 = sharedPreferences.getString("match_criterion", "")?.trim()
//        val criterion2 = sharedPreferences.getString("match_criterion_2", "")?.trim()
//        val criterion3 = sharedPreferences.getString("match_criterion_3", "")?.trim()
//        val minDistanceStr = sharedPreferences.getString("min_distance", "")?.trim()
//        val maxDistanceStr = sharedPreferences.getString("max_distance", "")?.trim()
//
//        val criteria = listOfNotNull(criterion1, criterion2, criterion3).filter { it.isNotEmpty() }
//        val minDistance = minDistanceStr?.toFloatOrNull()
//        val maxDistance = maxDistanceStr?.toFloatOrNull()
//
//        if (autoSelectEnabled && (criteria.isNotEmpty() || minDistance != null || maxDistance != null)) {
//            val rootNode = rootInActiveWindow
//            if (rootNode != null) {
//                val orderNodes = findOrderNodes(rootNode)
//                for (orderNode in orderNodes) {
//                    val distanceText = orderNode.findAccessibilityNodeInfosByText("km").firstOrNull()?.text?.toString()
//                    val distance = distanceText?.replace("km", "")?.trim()?.toFloatOrNull()
//                    val allTexts = getAllTexts(orderNode)
//
//                    // Kiểm tra khoảng cách
//                    if (distance != null &&
//                        (minDistance == null || distance >= minDistance) &&
//                        (maxDistance == null || distance <= maxDistance)) {
//                        // Kiểm tra các tiêu chí khác
//                        val matchingTexts = allTexts.filter { text ->
//                            criteria.any { criterion -> text.lowercase().contains(criterion.lowercase()) }
//                        }
//                        if (matchingTexts.isNotEmpty() || criteria.isEmpty()) {
//                            // In thông tin chi tiết của đơn hàng
//                            Log.d(TAG, "Đơn hàng khớp:")
//                            Log.d(TAG, " - Khoảng cách: $distance km")
//                            Log.d(TAG, " - Chi tiết: ${allTexts.joinToString(", ")}")
//
//                            val clicked = findAndClickNode(orderNode, criteria)
//                            if (clicked) {
//                                Log.d(TAG, "Đã nhấp vào đơn hàng với khoảng cách: $distance km")
//                            } else {
//                                Log.d(TAG, "Không nhấp được dù tìm thấy đơn hàng khớp")
//                            }
//                        }
//                    }
//                }
//            }
//        } else {
//            Log.d(TAG, "Chưa bật tự động chọn đơn hoặc tất cả tiêu chí rỗng")
//        }
//    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val eventPackageName = event?.packageName?.toString()
        val appPackageName = packageName

        // Kiểm tra xem sự kiện có từ ứng dụng chính không
        if (eventPackageName == appPackageName) {
            Log.d(TAG, "Sự kiện từ ứng dụng chính, không thực hiện hành động")
            return
        }

        // Lấy root node của màn hình hiện tại
        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            // In toàn bộ văn bản trên màn hình
            val allScreenTexts = getAllTexts(rootNode)
            Log.d(TAG, "Toàn bộ văn bản trên màn hình:")
            allScreenTexts.forEach { text ->
                Log.d(TAG, " - $text")
            }
            Log.d(TAG, "=============================")

            // Tìm và in thông tin các đơn hàng
            val orderNodes = findOrderNodes(rootNode)
            if (orderNodes.isNotEmpty()) {
                Log.d(TAG, "Tìm thấy ${orderNodes.size} đơn hàng trên màn hình")
                for ((index, orderNode) in orderNodes.withIndex()) {
                    val allTexts = getAllTexts(orderNode)
                    Log.d(TAG, "Đơn hàng ${index + 1}:")
                    allTexts.forEach { text ->
                        Log.d(TAG, " - $text")
                    }
                    Log.d(TAG, "-----------------------------")
                }
            } else {
                Log.d(TAG, "Không tìm thấy đơn hàng nào trên màn hình")
            }
        } else {
            Log.d(TAG, "Không lấy được root node của màn hình")
        }
    }

    private fun findOrderNodes(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val orderNodes = mutableListOf<AccessibilityNodeInfo>()
        val queue = mutableListOf(root)

        while (queue.isNotEmpty()) {
            val current = queue.removeAt(0)
            if (current.className == "android.view.ViewGroup" && current.childCount > 0) {
                val hasDistance = current.findAccessibilityNodeInfosByText("km").isNotEmpty()
                val hasActionButton = current.findAccessibilityNodeInfosByText("접점 받을게요").isNotEmpty()
                if (hasDistance && hasActionButton) {
                    orderNodes.add(current)
                }
            }
            for (i in 0 until current.childCount) {
                val child = current.getChild(i)
                if (child != null) {
                    queue.add(child)
                }
            }
        }
        return orderNodes
    }

    private fun findAndClickNode(node: AccessibilityNodeInfo, criteria: List<String>): Boolean {
        val text = node.text?.toString()?.lowercase()
        if (text != null && criteria.any { text.contains(it.lowercase()) }) {
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
                if (findAndClickNode(child, criteria)) {
                    return true
                }
            }
        }
        return false
    }

    private fun getAllTexts(node: AccessibilityNodeInfo): List<String> {
        val texts = mutableListOf<String>()
        val queue = mutableListOf(node)

        while (queue.isNotEmpty()) {
            val current = queue.removeAt(0)
            if (current.text?.isNotBlank() == true) {
                texts.add(current.text.toString())
            }
            for (i in 0 until current.childCount) {
                val child = current.getChild(i)
                if (child != null) {
                    queue.add(child)
                }
            }
        }
        return texts
    }

    override fun onInterrupt() {
        Log.d(TAG, "Dịch vụ bị gián đoạn")
    }
}