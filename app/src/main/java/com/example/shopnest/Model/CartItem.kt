package com.example.shopnest.Model

data class CartItem(
    val id: Int = 0,
    val userId: Long,
    val productId: Int,
    val name: String,
    val price: Double,
    val imagePath: String,
    val discount: Double = 0.0,
    val quantity: Int = 1
) {
    fun getDiscountedPrice(): Double {
        return price - (price * discount / 100)
    }
}