package com.example.automatically_receive_orders

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("AutoAcceptPrefs", MODE_PRIVATE)

        val matchCriterionEditText = findViewById<EditText>(R.id.order_match_criterion)
        val autoSelectSwitch = findViewById<Switch>(R.id.auto_select_enabled)
        val enableButton = findViewById<Button>(R.id.enable_button)

        // Tải giá trị đã lưu
        matchCriterionEditText.setText(sharedPreferences.getString("match_criterion", ""))
        autoSelectSwitch.isChecked = sharedPreferences.getBoolean("auto_select_enabled", false)

        // Lưu tiêu chí khi thay đổi
        matchCriterionEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                sharedPreferences.edit().putString("match_criterion", s.toString().trim()).apply()
            }
        })

        // Xử lý bật/tắt Switch
        autoSelectSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isAccessibilityServiceEnabled()) {
                autoSelectSwitch.isChecked = false
                promptEnableAccessibilityService()
            } else {
                sharedPreferences.edit().putBoolean("auto_select_enabled", isChecked).apply()
                Toast.makeText(
                    this,
                    if (isChecked) "Đã bật tự động chọn đơn" else "Đã tắt tự động chọn đơn",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Mở cài đặt Accessibility
        enableButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        val serviceName = "$packageName/${MyAccessibilityService::class.java.canonicalName}"
        return enabledServices?.contains(serviceName) == true
    }

    private fun promptEnableAccessibilityService() {
        AlertDialog.Builder(this)
            .setTitle("Yêu cầu bật Accessibility Service")
            .setMessage("Để sử dụng tính năng tự động chọn đơn, bạn cần bật Accessibility Service.")
            .setPositiveButton("OK") { _, _ -> startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
            .setNegativeButton("Hủy", null)
            .show()
    }
}