package com.example.automatically_receive_orders

data class FilterSetting(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val restaurantName: String,
    val restaurantAddress: String,
    val deliveryArea: String,
    val minDistance: String, // Khoảng cách tối thiểu
    val maxDistance: String  // Khoảng cách tối đa
)