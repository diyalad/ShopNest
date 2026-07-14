package com.example.shopnest.adapters

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopnest.Model.Subcategory
import com.example.shopnest.R
import java.io.File

class FavoriteProductsAdapter(
    private val favoriteProductsList: MutableList<Subcategory>,
    private val onItemClick: (Subcategory) -> Unit,
    private val onBuyNowClick: (Subcategory) -> Unit,
    private val onFavoriteClick: (Subcategory, Boolean) -> Unit
) : RecyclerView.Adapter<FavoriteProductsAdapter.FavoriteProductsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteProductsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_product, parent, false)
        return FavoriteProductsViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteProductsViewHolder, position: Int) {
        val favoriteProduct = favoriteProductsList[position]
        holder.bind(favoriteProduct)
        Log.d("FavoriteProductsAdapter", "Binding favorite product: ${favoriteProduct.name}")

        // Click listener for entire item
        holder.itemView.setOnClickListener {
            onItemClick(favoriteProduct)
        }

        // Click listener for "Buy Now" button
        holder.buyNowButton.setOnClickListener {
            onBuyNowClick(favoriteProduct)
        }

        // Click listener for favorite icon
        holder.favoriteImageView.setOnClickListener {
            // Always remove when delete icon is clicked
            onFavoriteClick(favoriteProduct, false)

            // Remove from list and notify adapter
            favoriteProductsList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, favoriteProductsList.size)
        }
    }

    override fun getItemCount(): Int = favoriteProductsList.size

    fun updateList(newList: List<Subcategory>) {
        favoriteProductsList.clear()
        favoriteProductsList.addAll(newList)
        notifyDataSetChanged()
    }

    class FavoriteProductsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val productDiscountPrice: TextView = itemView.findViewById(R.id.productDiscountPrice)
        private val productDetails: TextView = itemView.findViewById(R.id.productDetails)
        val buyNowButton: TextView = itemView.findViewById(R.id.buyNowButton)
        val favoriteImageView: ImageView = itemView.findViewById(R.id.favoriteImageView)

        fun bind(favoriteProduct: Subcategory) {
            productName.text = favoriteProduct.name
            productPrice.text = formatPrice(favoriteProduct.originalPrice)
            productDiscountPrice.text = formatPrice(favoriteProduct.discountedPrice)
            productDetails.text = buildDetailsText(favoriteProduct)

            // Load image from file path
            loadImageFromPath(favoriteProduct.imagePath)

            updateFavoriteButton(favoriteProduct.isFavorite)
        }

        private fun loadImageFromPath(imagePath: String?) {
            try {
                if (!imagePath.isNullOrEmpty()) {
                    val file = File(imagePath)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        productImage.setImageBitmap(bitmap)
                        Log.d("FavoriteAdapter", "Successfully loaded image from path: $imagePath")
                        return
                    }
                }
                productImage.setImageResource(R.drawable.ic_category_placeholder)
                Log.e("FavoriteAdapter", "Image path not found or invalid")
            } catch (e: Exception) {
                productImage.setImageResource(R.drawable.ic_category_placeholder)
                Log.e("FavoriteAdapter", "Error loading image: ${e.message}")
            }
        }

        private fun formatPrice(price: Double): String {
            return if (price % 1 == 0.0) {
                "₹${price.toInt()}"
            } else {
                "₹%.2f".format(price)
            }
        }

        private fun buildDetailsText(subcategory: Subcategory): String {
            return "${subcategory.discountPercentage}% off • ⭐${subcategory.rating} • ${subcategory.deliveryInfo}"
        }

        fun updateFavoriteButton(isFavorite: Boolean) {
            val favoriteIcon = if (isFavorite) {
                R.drawable.baseline_favorite_24
            } else {
                R.drawable.baseline_favorite_border_24
            }
            favoriteImageView.setImageResource(favoriteIcon)
        }
    }
}