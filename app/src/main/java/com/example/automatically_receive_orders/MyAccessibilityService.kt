package com.example.automatically_receive_orders

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {
    private val TAG = "MyAccessibilityService"
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("Tự động bắt đơn", MODE_PRIVATE)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d(TAG, "Sự kiện: $event")
        val rootNode = rootInActiveWindow ?: return

        // ✅ Kiểm tra nếu dịch vụ Accessibility chưa được kích hoạt
        if (!isAccessibilityServiceEnabled()) {
            Log.w(TAG, "Dịch vụ Accessibility chưa được bật, yêu cầu người dùng kích hoạt")
            openAccessibilitySettings()
            return
        }

        // Kiểm tra xem có cần lọc đơn không
        val filterEnabled = sharedPreferences.getBoolean("filter_enabled", false)

        if (filterEnabled) {
            // Tìm thông tin đơn hàng trên màn hình
            val restaurantName = sharedPreferences.getString("restaurant_name", "")?.trim() ?: ""
            val restaurantAddress = sharedPreferences.getString("restaurant_address", "")?.trim() ?: ""
            val deliveryArea = sharedPreferences.getString("delivery_area", "")?.trim() ?: ""

            // Nếu không có tiêu chí lọc nào được đặt, bỏ qua việc lọc
            if (restaurantName.isEmpty() && restaurantAddress.isEmpty() && deliveryArea.isEmpty()) {
                checkAndClickAcceptButton(rootNode)
                return
            }

            // Kiểm tra nếu thông tin trên màn hình phù hợp với tiêu chí
            val matchesFilter = checkIfOrderMatchesFilters(rootNode, restaurantName, restaurantAddress, deliveryArea)

            if (matchesFilter) {
                checkAndClickAcceptButton(rootNode)
                Log.d(TAG, "Đơn hàng phù hợp với bộ lọc - Đã nhấn nhận đơn")
            } else {
                Log.d(TAG, "Đơn hàng không phù hợp với bộ lọc - Bỏ qua")
            }
        } else {
            // Nếu không bật lọc, nhận tất cả các đơn
            checkAndClickAcceptButton(rootNode)
        }
    }

    private fun checkIfOrderMatchesFilters(
        rootNode: AccessibilityNodeInfo,
        restaurantName: String,
        restaurantAddress: String,
        deliveryArea: String
    ): Boolean {
        var nameMatches = true
        var addressMatches = true
        var areaMatches = true

        // Chỉ kiểm tra tên nếu đã thiết lập
        if (restaurantName.isNotEmpty()) {
            val nameKeywords = restaurantName.split(" ")
            nameMatches = nameKeywords.all { keyword ->
                rootNode.findAccessibilityNodeInfosByText(keyword).isNotEmpty()
            }
            Log.d(TAG, "Kiểm tra tên quán: $nameMatches - $restaurantName")
        }

        // Chỉ kiểm tra địa chỉ nếu đã thiết lập
        if (restaurantAddress.isNotEmpty()) {
            val addressKeywords = restaurantAddress.split(" ")
            addressMatches = addressKeywords.all { keyword ->
                rootNode.findAccessibilityNodeInfosByText(keyword).isNotEmpty()
            }
            Log.d(TAG, "Kiểm tra địa chỉ quán: $addressMatches - $restaurantAddress")
        }

        // Chỉ kiểm tra khu vực giao nếu đã thiết lập
        if (deliveryArea.isNotEmpty()) {
            val areaKeywords = deliveryArea.split(" ")
            areaMatches = areaKeywords.all { keyword ->
                rootNode.findAccessibilityNodeInfosByText(keyword).isNotEmpty()
            }
            Log.d(TAG, "Kiểm tra khu vực giao: $areaMatches - $deliveryArea")
        }

        // Cả ba đều phải khớp (nếu được thiết lập)
        return nameMatches && addressMatches && areaMatches
    }

    private fun checkAndClickAcceptButton(rootNode: AccessibilityNodeInfo) {
        // Tìm nút có văn bản "Nhận đơn"
        val nodes = rootNode.findAccessibilityNodeInfosByText("Nhận đơn")
        for (node in nodes) {
            if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "Đã nhấn vào nút Nhận đơn")
                break
            }
        }
    }

    // ✅ Thêm hàm kiểm tra Accessibility Service có được bật không
    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(AccessibilityManager::class.java)
        val enabledServices =
            Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val colonSplitter = enabledServices?.split(":") ?: return false
        return colonSplitter.any { it.contains(packageName + "/.MyAccessibilityService") }
    }

    // ✅ Nếu dịch vụ chưa được bật, mở cài đặt Accessibility
    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onInterrupt() {
        Log.d(TAG, "Dịch vụ bị gián đoạn")
    }
}
