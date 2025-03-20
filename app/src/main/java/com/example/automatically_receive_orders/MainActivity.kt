package com.example.automatically_receive_orders

import android.app.Activity
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    companion object {
        const val REQUEST_CODE_APPLY_FILTER = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.open_saved_filters_button).setOnClickListener {
            val intent = Intent(this, SavedFiltersActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_APPLY_FILTER)
        }

        sharedPreferences = getSharedPreferences("AutoAcceptPrefs", MODE_PRIVATE)

        val matchCriterionEditText = findViewById<EditText>(R.id.order_match_criterion)
        val matchCriterionEditText2 = findViewById<EditText>(R.id.order_match_criterion_2)
        val matchCriterionEditText3 = findViewById<EditText>(R.id.order_match_criterion_3)
        val minDistanceEditText = findViewById<EditText>(R.id.min_distance)
        val maxDistanceEditText = findViewById<EditText>(R.id.max_distance)
        val autoSelectSwitch = findViewById<Switch>(R.id.auto_select_enabled)
        val enableButton = findViewById<Button>(R.id.enable_button)

        // Tải giá trị đã lưu
        matchCriterionEditText.setText(sharedPreferences.getString("match_criterion", ""))
        matchCriterionEditText2.setText(sharedPreferences.getString("match_criterion_2", ""))
        matchCriterionEditText3.setText(sharedPreferences.getString("match_criterion_3", ""))
        minDistanceEditText.setText(sharedPreferences.getString("min_distance", ""))
        maxDistanceEditText.setText(sharedPreferences.getString("max_distance", ""))
        autoSelectSwitch.isChecked = sharedPreferences.getBoolean("auto_select_enabled", false)

        // Lưu giá trị khi thay đổi
        matchCriterionEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                sharedPreferences.edit().putString("match_criterion", s.toString().trim()).apply()
            }
        })

        matchCriterionEditText2.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                sharedPreferences.edit().putString("match_criterion_2", s.toString().trim()).apply()
            }
        })

        matchCriterionEditText3.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                sharedPreferences.edit().putString("match_criterion_3", s.toString().trim()).apply()
            }
        })

        minDistanceEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                sharedPreferences.edit().putString("min_distance", s.toString().trim()).apply()
            }
        })

        maxDistanceEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                sharedPreferences.edit().putString("max_distance", s.toString().trim()).apply()
            }
        })

        autoSelectSwitch.setOnCheckedChangeListener { _, isChecked ->
            val criterion1 = sharedPreferences.getString("match_criterion", "")?.trim()
            val criterion2 = sharedPreferences.getString("match_criterion_2", "")?.trim()
            val criterion3 = sharedPreferences.getString("match_criterion_3", "")?.trim()
            val minDistance = sharedPreferences.getString("min_distance", "")?.trim()
            val maxDistance = sharedPreferences.getString("max_distance", "")?.trim()
            val hasCriteria = !criterion1.isNullOrEmpty() || !criterion2.isNullOrEmpty() ||
                    !criterion3.isNullOrEmpty() || !minDistance.isNullOrEmpty() ||
                    !maxDistance.isNullOrEmpty()

            if (isChecked && !isAccessibilityServiceEnabled()) {
                autoSelectSwitch.isChecked = false
                promptEnableAccessibilityService()
            } else if (isChecked && !hasCriteria) {
                autoSelectSwitch.isChecked = false
                Toast.makeText(this, "Vui lòng nhập ít nhất một tiêu chí trước khi bật", Toast.LENGTH_SHORT).show()
            } else {
                sharedPreferences.edit().putBoolean("auto_select_enabled", isChecked).apply()
                Toast.makeText(this, if (isChecked) "Đã bật tự động chọn đơn" else "Đã tắt tự động chọn đơn", Toast.LENGTH_SHORT).show()
            }
        }

        enableButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.save_filter_button).setOnClickListener {
            val restaurantName = findViewById<EditText>(R.id.order_match_criterion).text.toString().trim()
            val restaurantAddress = findViewById<EditText>(R.id.order_match_criterion_2).text.toString().trim()
            val deliveryArea = findViewById<EditText>(R.id.order_match_criterion_3).text.toString().trim()
            val minDistance = findViewById<EditText>(R.id.min_distance).text.toString().trim()
            val maxDistance = findViewById<EditText>(R.id.max_distance).text.toString().trim()

            if (restaurantName.isEmpty() && restaurantAddress.isEmpty() && deliveryArea.isEmpty() &&
                minDistance.isEmpty() && maxDistance.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập ít nhất một tiêu chí", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val filtersJson = sharedPreferences.getString("saved_filters", "[]")
            val type = object : TypeToken<MutableList<FilterSetting>>() {}.type
            val filterSettings: MutableList<FilterSetting> = gson.fromJson(filtersJson, type)

            val name = "Bộ lọc ${filterSettings.size + 1}"
            val filterSetting = FilterSetting(
                name = name,
                restaurantName = restaurantName,
                restaurantAddress = restaurantAddress,
                deliveryArea = deliveryArea,
                minDistance = minDistance,
                maxDistance = maxDistance
            )

            filterSettings.add(filterSetting)
            val newFiltersJson = gson.toJson(filterSettings)
            sharedPreferences.edit().putString("saved_filters", newFiltersJson).apply()

            Toast.makeText(this, "Đã lưu bộ lọc: $name", Toast.LENGTH_SHORT).show()

            findViewById<EditText>(R.id.order_match_criterion).text.clear()
            findViewById<EditText>(R.id.order_match_criterion_2).text.clear()
            findViewById<EditText>(R.id.order_match_criterion_3).text.clear()
            findViewById<EditText>(R.id.min_distance).text.clear()
            findViewById<EditText>(R.id.max_distance).text.clear()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_APPLY_FILTER && resultCode == Activity.RESULT_OK) {
            val restaurantName = sharedPreferences.getString("match_criterion", "")
            val restaurantAddress = sharedPreferences.getString("match_criterion_2", "")
            val deliveryArea = sharedPreferences.getString("match_criterion_3", "")
            val minDistance = sharedPreferences.getString("min_distance", "")
            val maxDistance = sharedPreferences.getString("max_distance", "")

            findViewById<EditText>(R.id.order_match_criterion).setText(restaurantName)
            findViewById<EditText>(R.id.order_match_criterion_2).setText(restaurantAddress)
            findViewById<EditText>(R.id.order_match_criterion_3).setText(deliveryArea)
            findViewById<EditText>(R.id.min_distance).setText(minDistance)
            findViewById<EditText>(R.id.max_distance).setText(maxDistance)

            Toast.makeText(this, "Đã áp dụng bộ lọc", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
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