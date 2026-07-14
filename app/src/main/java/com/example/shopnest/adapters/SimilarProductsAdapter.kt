package com.example.shopnest.adapters

import android.graphics.BitmapFactory
import android.graphics.Paint
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

class SimilarProductsAdapter(
    private val products: List<Subcategory>,
    private val onItemClick: (Subcategory) -> Unit,
    private val onFavoriteClick: (Subcategory, Boolean) -> Unit,
    private val onAddToCart: (Subcategory) -> Unit
) : RecyclerView.Adapter<SimilarProductsAdapter.ViewHolder>() {

    // Image load listener interface
    interface ImageLoadListener {
        fun onImageLoadSuccess(position: Int, product: Subcategory)
        fun onImageLoadFailure(position: Int, product: Subcategory, error: String)
    }

    private var imageLoadListener: ImageLoadListener? = null

    fun setImageLoadListener(listener: ImageLoadListener) {
        this.imageLoadListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        val originalPrice: TextView = itemView.findViewById(R.id.originalPrice)
        val discountText: TextView = itemView.findViewById(R.id.discountText)
        val favoriteIcon: ImageView = itemView.findViewById(R.id.favoriteIcon)
        val ratingText: TextView = itemView.findViewById(R.id.ratingText)
        val addToCartButton: TextView = itemView.findViewById(R.id.addToCartButton)
        val deliveryInfoText: TextView = itemView.findViewById(R.id.deliveryInfo)

        fun bind(product: Subcategory) {
            // Load image from file path
            try {
                val file = File(product.imagePath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    productImage.setImageBitmap(bitmap)
                    imageLoadListener?.onImageLoadSuccess(adapterPosition, product)
                } else {
                    productImage.setImageResource(R.drawable.ic_category_placeholder)
                    imageLoadListener?.onImageLoadFailure(
                        adapterPosition,
                        product,
                        "Image file not found: ${product.imagePath}"
                    )
                    Log.w("SimilarProductsAdapter", "Image file not found: ${product.imagePath}")
                }
            } catch (e: Exception) {
                productImage.setImageResource(R.drawable.ic_category_placeholder)
                imageLoadListener?.onImageLoadFailure(
                    adapterPosition,
                    product,
                    e.message ?: "Unknown error loading image"
                )
                Log.e("SimilarProductsAdapter", "Error loading product image", e)
            }

            // Set product information
            productName.text = product.name
            productPrice.text = "₹${product.discountedPrice.toInt()}"
            originalPrice.text = "₹${product.originalPrice.toInt()}"
            originalPrice.paintFlags = originalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            discountText.text = "${product.discountPercentage}% OFF"
            ratingText.text = "%.1f ⭐".format(product.rating)
            deliveryInfoText.text = product.deliveryInfo

            // Update favorite icon
            updateFavoriteIcon(product.isFavorite)

            // Set click listeners
            itemView.setOnClickListener { onItemClick(product) }
            favoriteIcon.setOnClickListener {
                val isFavorite = !product.isFavorite
                product.isFavorite = isFavorite
                onFavoriteClick(product, isFavorite)
                updateFavoriteIcon(isFavorite)
            }
            addToCartButton.setOnClickListener {
                onAddToCart(product)
            }
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            favoriteIcon.setImageResource(
                if (isFavorite) R.drawable.baseline_favorite_24
                else R.drawable.baseline_favorite_border_24
            )
            favoriteIcon.setColorFilter(
                itemView.context.getColor(
                    if (isFavorite) R.color.red
                    else R.color.gray
                )
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_similar_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size
}