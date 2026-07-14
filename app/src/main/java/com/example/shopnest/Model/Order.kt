package com.example.shopnest.Model

data class Order(
    val orderId: Long,
    val userId: Long,
    val subcategoryId: Int,
    val quantity: Int,
    val totalPrice: Double,
    val paymentId: String
)
