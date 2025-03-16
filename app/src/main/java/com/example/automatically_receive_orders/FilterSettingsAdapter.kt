package com.example.automatically_receive_orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FilterSettingsAdapter(
    private val filterSettings: List<FilterSetting>,
    private val onApplyClick: (FilterSetting) -> Unit,
    private val onDeleteClick: (FilterSetting) -> Unit
) : RecyclerView.Adapter<FilterSettingsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.filter_name_text)
        val detailsText: TextView = view.findViewById(R.id.filter_details_text)
        val applyButton: Button = view.findViewById(R.id.apply_button)
        val deleteButton: Button = view.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_filter_setting, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val filterSetting = filterSettings[position]

        holder.nameText.text = filterSetting.name

        val details = StringBuilder()
        if (filterSetting.restaurantName.isNotEmpty()) {
            details.append("Tên quán: ${filterSetting.restaurantName}\n")
        }
        if (filterSetting.restaurantAddress.isNotEmpty()) {
            details.append("Địa chỉ quán: ${filterSetting.restaurantAddress}\n")
        }
        if (filterSetting.deliveryArea.isNotEmpty()) {
            details.append("Khu vực giao: ${filterSetting.deliveryArea}")
        }

        holder.detailsText.text = details.toString()

        holder.applyButton.setOnClickListener {
            onApplyClick(filterSetting)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(filterSetting)
        }
    }

    override fun getItemCount() = filterSettings.size
}