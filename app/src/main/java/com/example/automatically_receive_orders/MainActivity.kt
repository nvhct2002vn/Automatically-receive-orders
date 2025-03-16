package com.example.automatically_receive_orders

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("AutoAcceptPrefs", MODE_PRIVATE)

        // Ánh xạ các phần tử UI
        val filterNameEditText = findViewById<EditText>(R.id.filter_name)
        val restaurantNameEditText = findViewById<EditText>(R.id.restaurant_name)
        val restaurantAddressEditText = findViewById<EditText>(R.id.restaurant_address)
        val deliveryAreaEditText = findViewById<EditText>(R.id.delivery_area)
        val filterEnabledSwitch = findViewById<Switch>(R.id.filter_enabled)
        val saveFilterButton = findViewById<Button>(R.id.save_filter)
        val showSavedFiltersButton = findViewById<Button>(R.id.show_saved_filters)
        val enableButton = findViewById<Button>(R.id.enable_button)

        // Tải thiết lập hiện tại
        restaurantNameEditText.setText(sharedPreferences.getString("restaurant_name", ""))
        restaurantAddressEditText.setText(sharedPreferences.getString("restaurant_address", ""))
        deliveryAreaEditText.setText(sharedPreferences.getString("delivery_area", ""))
        filterNameEditText.setText(sharedPreferences.getString("current_filter_name", ""))
        filterEnabledSwitch.isChecked = sharedPreferences.getBoolean("filter_enabled", false)

        // Lưu thiết lập hiện tại
        saveFilterButton.setOnClickListener {
            val filterName = filterNameEditText.text.toString().trim()
            val restaurantName = restaurantNameEditText.text.toString().trim()
            val restaurantAddress = restaurantAddressEditText.text.toString().trim()
            val deliveryArea = deliveryAreaEditText.text.toString().trim()

            if (filterName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên cho thiết lập", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Lưu thiết lập hiện tại
            val editor = sharedPreferences.edit()
            editor.putString("current_filter_name", filterName)
            editor.putString("restaurant_name", restaurantName)
            editor.putString("restaurant_address", restaurantAddress)
            editor.putString("delivery_area", deliveryArea)
            editor.putBoolean("filter_enabled", filterEnabledSwitch.isChecked)
            editor.apply()

            // Thêm thiết lập vào danh sách đã lưu
            val newFilter = FilterSetting(
                name = filterName,
                restaurantName = restaurantName,
                restaurantAddress = restaurantAddress,
                deliveryArea = deliveryArea
            )

            // Tải danh sách hiện có
            val filtersJson = sharedPreferences.getString("saved_filters", "[]")
            val type = object : TypeToken<MutableList<FilterSetting>>() {}.type
            val filterSettings: MutableList<FilterSetting> = gson.fromJson(filtersJson, type)

            // Kiểm tra xem tên đã tồn tại chưa
            val existingIndex = filterSettings.indexOfFirst { it.name == filterName }
            if (existingIndex >= 0) {
                // Cập nhật thiết lập hiện có
                filterSettings[existingIndex] = newFilter
            } else {
                // Thêm thiết lập mới
                filterSettings.add(newFilter)
            }

            // Lưu danh sách đã cập nhật
            val updatedFiltersJson = gson.toJson(filterSettings)
            editor.putString("saved_filters", updatedFiltersJson)
            editor.apply()

            Toast.makeText(this, "Đã lưu thiết lập thành công", Toast.LENGTH_SHORT).show()
        }

        // Hiển thị danh sách đã lưu
        showSavedFiltersButton.setOnClickListener {
            val intent = Intent(this, SavedFiltersActivity::class.java)
            startActivity(intent)
        }

        // Mở cài đặt Accessibility
        enableButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }

    // Kiểm tra dịch vụ có đang chạy không
    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices =
            Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val colonSplitter = enabledServices?.split(":") ?: return false
        return colonSplitter.any { it.contains(packageName + "/.MyAccessibilityService") }
    }
}