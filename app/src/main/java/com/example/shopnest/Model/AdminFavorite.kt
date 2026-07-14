package com.example.shopnest.Model

import android.os.Parcel
import android.os.Parcelable

data class AdminFavorite(
    val favoriteId: Int,
    val userId: Int,
    val subcategory: Subcategory
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readParcelable<Subcategory>(Subcategory::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(favoriteId)
        parcel.writeInt(userId)
        parcel.writeParcelable(subcategory, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AdminFavorite> {
        override fun createFromParcel(parcel: Parcel): AdminFavorite {
            return AdminFavorite(parcel)
        }

        override fun newArray(size: Int): Array<AdminFavorite?> {
            return arrayOfNulls(size)
        }
    }
}
