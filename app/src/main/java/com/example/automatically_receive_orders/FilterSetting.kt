package com.example.automatically_receive_orders

data class FilterSetting(
    val id: Long = System.currentTimeMillis(), // ID duy nhất dựa trên thời gian
    val name: String, // Tên cho thiết lập lọc
    val restaurantName: String,
    val restaurantAddress: String,
    val deliveryArea: String // Khu vực giao hàng
)