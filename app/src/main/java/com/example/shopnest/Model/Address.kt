package com.example.shopnest.Model

data class Address(
    val addressId: Long,
    val fullName: String,
    val phone: String,
    val alternatePhone: String,
    val pincode: String,
    val state: String,
    val city: String,
    val houseNo: String,
    val roadName: String,
    val userId: Long
)