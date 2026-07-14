package com.example.shopnest.Model

import android.os.Parcel
import android.os.Parcelable

data class Subcategory(
    val id: Int,
    val name: String,
    val imagePath: String, // Changed from ByteArray? to String
    val categoryId: Int,
    val discountedPrice: Double,
    val originalPrice: Double,
    val discountPercentage: Int,
    val deliveryInfo: String,
    val details: String,
    val rating: Float,
    var isFavorite: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "", // Changed to readString
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readFloat(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(imagePath) // Changed to writeString
        parcel.writeInt(categoryId)
        parcel.writeDouble(discountedPrice)
        parcel.writeDouble(originalPrice)
        parcel.writeInt(discountPercentage)
        parcel.writeString(deliveryInfo)
        parcel.writeString(details)
        parcel.writeFloat(rating)
        parcel.writeByte(if (isFavorite) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Subcategory> {
        override fun createFromParcel(parcel: Parcel): Subcategory {
            return Subcategory(parcel)
        }

        override fun newArray(size: Int): Array<Subcategory?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "Subcategory(id=$id, name='$name', imagePath='$imagePath', categoryId=$categoryId, " +
                "discountedPrice=$discountedPrice, originalPrice=$originalPrice, " +
                "discountPercentage=$discountPercentage, deliveryInfo='$deliveryInfo', " +
                "details='$details', rating=$rating, isFavorite=$isFavorite)"
    }
}