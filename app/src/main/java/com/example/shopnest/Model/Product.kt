package com.example.shopnest.Model

import android.os.Parcel
import android.os.Parcelable

data class Product(
    val id: Int,
    val name: String,
    val discountedPrice: Double,
    val originalPrice: Double,
    val discountPercentage: Int,
    val imageResId: Int,                  // Integer resource ID
    val imagePaths: List<String>,         // Multiple image paths (List<String>)
    val deliveryInfo: String,
    val details: String,
    val rating: Float
) : Parcelable {
    // Write to Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeDouble(discountedPrice)
        parcel.writeDouble(originalPrice)
        parcel.writeInt(discountPercentage)
        parcel.writeInt(imageResId)
        parcel.writeStringList(imagePaths)
        parcel.writeString(deliveryInfo)
        parcel.writeString(details)
        parcel.writeFloat(rating)
    }

    // Describe contents (usually returns 0)
    override fun describeContents(): Int = 0

    // Companion object to create from Parcel
    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(
                parcel.readInt(),
                parcel.readString() ?: "",
                parcel.readDouble(),
                parcel.readDouble(),
                parcel.readInt(),
                parcel.readInt(),
                parcel.createStringArrayList() ?: listOf(),
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readFloat()
            )
        }

        override fun newArray(size: Int): Array<Product?> = arrayOfNulls(size)
    }
}