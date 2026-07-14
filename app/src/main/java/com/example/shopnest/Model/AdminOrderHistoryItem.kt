package com.example.shopnest.Model

data class AdminOrderHistoryItem(
    val orderId: Int,
    val userId: Int,
    val userName: String,
    val userEmail: String,
    val productId: Int,
    val productName: String,
    val productImageResId: Int,
    val quantity: Int,
    val totalPrice: Double,
    val paymentId: String,
    val paymentStatus: String,
    val deliveryStartDate: String,
    val deliveryEndDate: String,
    val size: String,
    val address: String,
    val orderDate: String
) {
    fun formattedTotalPrice(): String = "₹${"%.2f".format(totalPrice)}"
    fun formattedOrderDate(): String = "📅 $orderDate"
    fun formattedDeliveryPeriod(): String = "🚚 $deliveryStartDate to $deliveryEndDate"
}
