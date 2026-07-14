package com.example.shopnest.Model

data class OrderHistory(
    val orderHistoryId: Long,
    val userId: Long,
    val subcategoryId: Int,
    val quantity: Int,
    val totalPrice: Double,
    val paymentId: String,
    val paymentStatus: String,
    val deliveryStartDate: String,
    val deliveryEndDate: String,
    val size: String,
    val addressId: Long,
    val productName: String = "",
    val productImagePath: String = "", // Changed from Int to String
    val productRating: Float = 0.0f,
    val deliveryAddress: String = "",
    val orderDate: String = ""
)