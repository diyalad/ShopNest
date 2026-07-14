package com.example.shopnest.Model

import android.graphics.Bitmap

data class Profile(
    val profileId: Long,
    val userId: Long,
    val fullName: String,
    val phone: String,
    val email: String,
    val gender: String,
    val pincode: String,
    val city: String,
    val state: String,
    val dob: String,
    val maritalStatus: String,
    val education: String,
    val income: String,
    val profileImagePath: String?
)
