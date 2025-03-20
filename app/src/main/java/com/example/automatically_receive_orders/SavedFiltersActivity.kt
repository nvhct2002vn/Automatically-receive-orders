package com.example.automatically_receive_orders

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SavedFiltersActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    private lateinit var filterSettings: MutableList<FilterSetting>
    private lateinit var adapter: FilterSettingsAdapter
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_filters)

        sharedPreferences = getSharedPreferences("AutoAcceptPrefs", MODE_PRIVATE)
        recyclerView = findViewById(R.id.filters_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadFilterSettings()

        adapter = FilterSettingsAdapter(
            filterSettings,
            onApplyClick = { filter -> applyFilter(filter) },
            onDeleteClick = { filter -> deleteFilter(filter) }
        )

        recyclerView.adapter = adapter
    }

    private fun loadFilterSettings() {
        val filtersJson = sharedPreferences.getString("saved_filters", "[]")
        val type = object : TypeToken<MutableList<FilterSetting>>() {}.type
        filterSettings = gson.fromJson(filtersJson, type)
    }

    private fun applyFilter(filter: FilterSetting) {
        val editor = sharedPreferences.edit()
        editor.putString("current_filter_name", filter.name)
        editor.putString("restaurant_name", filter.restaurantName)
        editor.putString("restaurant_address", filter.restaurantAddress)
        editor.putString("delivery_area", filter.deliveryArea)
        editor.putString("min_distance", filter.minDistance)
        editor.putString("max_distance", filter.maxDistance)
        editor.putBoolean("filter_enabled", true)

        editor.putString("match_criterion", filter.restaurantName)
        editor.putString("match_criterion_2", filter.restaurantAddress)
        editor.putString("match_criterion_3", filter.deliveryArea)
        editor.putString("min_distance", filter.minDistance)
        editor.putString("max_distance", filter.maxDistance)
        editor.apply()

        setResult(Activity.RESULT_OK)
        finish()

        Toast.makeText(this, "Đã áp dụng bộ lọc: ${filter.name}", Toast.LENGTH_SHORT).show()
    }

    private fun deleteFilter(filter: FilterSetting) {
        filterSettings.removeAll { it.id == filter.id }
        saveFilterSettings()
        adapter.notifyDataSetChanged()
        Toast.makeText(this, "Đã xóa thiết lập", Toast.LENGTH_SHORT).show()
    }

    private fun saveFilterSettings() {
        val filtersJson = gson.toJson(filterSettings)
        val editor = sharedPreferences.edit()
        editor.putString("saved_filters", filtersJson)
        editor.apply()
    }
}