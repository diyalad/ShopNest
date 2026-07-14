// AdminCartItem.kt
package com.example.shopnest.Model

data class AdminCartItem(
    // Base cart fields
    val id: Int,
    val userId: Int,
    val productId: Int,
    val productName: String,
    val productPrice: Double,
    val productImageResId: Int,
    val discount: Double,
    val quantity: Int,

    // Admin-specific fields
    val userName: String = "",          // Added for admin view
    val userEmail: String = "",         // Added for admin view
    val dateAdded: String = "",         // When item was added to cart
    val isCheckedOut: Boolean = false,  // If the cart item was purchased
    val lastModified: String = ""       // Last update timestamp
) {
    // Calculated property for total price
    val totalPrice: Double
        get() = (productPrice - discount) * quantity

    // Formatted string for display
    fun formattedTotalPrice(): String {
        return "₹${"%.2f".format(totalPrice)}"
    }

    // Formatted discount string
    fun formattedDiscount(): String {
        return if (discount > 0) "-₹${"%.2f".format(discount)}" else ""
    }
}