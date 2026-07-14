package com.example.shopnest.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shopnest.Model.AdminFavorite
import com.example.shopnest.R
import com.example.shopnest.databinding.ItemAdminFavoriteBinding
import java.io.File

class AdminFavoritesAdapter(
    private val favorites: List<AdminFavorite>,
    private val onItemClick: (subcategoryId: Int) -> Unit = {},
    private val onFavoriteToggle: (subcategoryId: Int, isFavorite: Boolean) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<AdminFavoritesAdapter.AdminFavoriteViewHolder>() {

    inner class AdminFavoriteViewHolder(val binding: ItemAdminFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(favorite: AdminFavorite) {
            val subcategory = favorite.subcategory

            // Load image from path
            loadImageFromPath(subcategory.imagePath)

            // Set text values
            binding.favoriteItemName.text = subcategory.name
            binding.favoriteDiscountedPrice.text = formatPrice(subcategory.discountedPrice)
            binding.favoriteOriginalPrice.text = formatPrice(subcategory.originalPrice)
            binding.favoriteDiscountPercentage.text =
                itemView.context.getString(R.string.discount_price, subcategory.discountPercentage)
            binding.favoriteItemRating.rating = subcategory.rating
            binding.favoriteDeliveryInfo.text = subcategory.deliveryInfo

            // Set favorite icon state
            binding.favoriteItemImage.setImageResource(
                if (subcategory.isFavorite) R.drawable.baseline_favorite_24
                else R.drawable.baseline_favorite_border_24
            )

            // Set click listeners
            binding.root.setOnClickListener {
                onItemClick(subcategory.id)
            }

//            binding.favoriteButton.setOnClickListener {
//                val newFavoriteState = !subcategory.isFavorite
//                subcategory.isFavorite = newFavoriteState
//                binding.favoriteButton.setImageResource(
//                    if (newFavoriteState) R.drawable.ic_favorite_filled
//                    else R.drawable.ic_favorite_border
//                )
//                onFavoriteToggle(subcategory.id, newFavoriteState)
//            }
        }

        private fun loadImageFromPath(imagePath: String) {
            try {
                if (imagePath.isNotEmpty()) {
                    val file = File(imagePath)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        binding.favoriteItemImage.setImageBitmap(bitmap)
                        return
                    }
                }
                binding.favoriteItemImage.setImageResource(R.drawable.ic_category_placeholder)
            } catch (e: Exception) {
                binding.favoriteItemImage.setImageResource(R.drawable.ic_category_placeholder)
            }
        }

        private fun formatPrice(price: Double): String {
            return if (price % 1 == 0.0) {
                "₹${price.toInt()}"
            } else {
                "₹%.2f".format(price)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminFavoriteViewHolder {
        val binding = ItemAdminFavoriteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminFavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminFavoriteViewHolder, position: Int) {
        holder.bind(favorites[position])
    }

    override fun getItemCount(): Int = favorites.size
}