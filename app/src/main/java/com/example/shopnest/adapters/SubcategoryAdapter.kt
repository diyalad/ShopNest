package com.example.shopnest.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.shopnest.Model.Subcategory
import com.example.shopnest.R
import java.io.File
import java.io.FileInputStream

class SubcategoryAdapter(
    private val subcategoryList: List<Subcategory>,
    private val onItemClick: (Subcategory) -> Unit = {},
    private val onBuyNowClick: (Subcategory) -> Unit = {},
    private val onFavoriteClick: (Subcategory, Boolean) -> Unit = { _, _ -> },
    private val onAddToCart: (Subcategory) -> Unit = {}
) : RecyclerView.Adapter<SubcategoryAdapter.SubcategoryViewHolder>() {

    companion object {
        private const val TAG = "SubcategoryAdapter"
        private const val MAX_IMAGE_SIZE = 2048 // Max width/height in pixels
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubcategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return SubcategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubcategoryViewHolder, position: Int) {
        val subcategory = subcategoryList[position]
        holder.bind(subcategory)

        Log.d(TAG, "Binding subcategory: ${subcategory.name} (ID: ${subcategory.id})")
        Log.d(TAG, "Image path: ${subcategory.imagePath}")

        holder.itemView.setOnClickListener { onItemClick(subcategory) }
        holder.favoriteImageView?.setOnClickListener {
            val isFavorite = !subcategory.isFavorite
            subcategory.isFavorite = isFavorite
            onFavoriteClick(subcategory, isFavorite)
            holder.updateFavoriteButton(isFavorite)
        }
        holder.addToCartButton?.setOnClickListener { onAddToCart(subcategory) }
    }

    override fun getItemCount(): Int = subcategoryList.size

    inner class SubcategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView? = itemView.findViewById(R.id.productName)
        private val productImage: ImageView? = itemView.findViewById(R.id.productImage)
        private val discountedPrice: TextView? = itemView.findViewById(R.id.discountedPriceText)
        private val originalPrice: TextView? = itemView.findViewById(R.id.originalPriceText)
        private val discountPercentage: TextView? = itemView.findViewById(R.id.discountText)
        private val ratingText: TextView? = itemView.findViewById(R.id.ratingText)
        private val deliveryInfoText: TextView? = itemView.findViewById(R.id.deliveryInfo)
        private val detailsText: TextView? = itemView.findViewById(R.id.deliveryText)
        val favoriteImageView: ImageView? = itemView.findViewById(R.id.favoriteIcon)
        val addToCartButton: Button? = itemView.findViewById(R.id.addToCartButton)

        fun bind(subcategory: Subcategory) {
            productName?.text = subcategory.name
            loadImageWithLogging(subcategory.imagePath)
            setPriceInfo(subcategory)
            setRatingInfo(subcategory)
            setAdditionalInfo(subcategory)
            updateFavoriteButton(subcategory.isFavorite)
        }

        private fun loadImageWithLogging(imagePath: String?) {
            try {
                if (imagePath.isNullOrEmpty()) {
                    Log.w(TAG, "Empty image path, using placeholder")
                    productImage?.setImageResource(R.drawable.ic_category_placeholder)
                    return
                }

                val file = File(imagePath)
                if (!file.exists()) {
                    Log.w(TAG, "Image file not found at: $imagePath")
                    productImage?.setImageResource(R.drawable.ic_category_placeholder)
                    return
                }

                Log.d(TAG, "Loading image from: $imagePath (Size: ${file.length()} bytes)")

                // Calculate inSampleSize for memory optimization
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(imagePath, options)

                options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                options.inJustDecodeBounds = false

                // Try to load with FileInputStream to avoid FileUriExposedException
                FileInputStream(file).use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream, null, options)
                    if (bitmap != null) {
                        productImage?.setImageBitmap(bitmap)
                        Log.d(TAG, "Image loaded successfully (Dimensions: ${bitmap.width}x${bitmap.height})")
                    } else {
                        Log.e(TAG, "Failed to decode bitmap")
                        productImage?.setImageResource(R.drawable.ic_category_placeholder)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading image: ${e.message}", e)
                productImage?.setImageResource(R.drawable.ic_category_placeholder)
            }
        }

        private fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int,
            reqHeight: Int
        ): Int {
            val (height: Int, width: Int) = options.run { outHeight to outWidth }
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {
                val halfHeight: Int = height / 2
                val halfWidth: Int = width / 2

                while (halfHeight / inSampleSize >= reqHeight &&
                    halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }

        private fun setPriceInfo(subcategory: Subcategory) {
            discountedPrice?.text = formatPrice(subcategory.discountedPrice)
            originalPrice?.text = formatPrice(subcategory.originalPrice)
            originalPrice?.paintFlags = originalPrice?.paintFlags?.or(Paint.STRIKE_THRU_TEXT_FLAG) ?: 0
            discountPercentage?.text = "${subcategory.discountPercentage}% off"
        }

        private fun setRatingInfo(subcategory: Subcategory) {
            ratingText?.text = "%.1f ⭐".format(subcategory.rating)
        }

        private fun setAdditionalInfo(subcategory: Subcategory) {
            deliveryInfoText?.text = subcategory.deliveryInfo
            detailsText?.text = subcategory.details
        }

        private fun formatPrice(price: Double): String {
            return if (price % 1 == 0.0) {
                "₹${price.toInt()}"
            } else {
                "₹%.2f".format(price)
            }
        }

        fun updateFavoriteButton(isFavorite: Boolean) {
            favoriteImageView?.setImageResource(
                if (isFavorite) R.drawable.baseline_favorite_24
                else R.drawable.baseline_favorite_border_24
            )
            favoriteImageView?.setColorFilter(
                ContextCompat.getColor(
                    itemView.context,
                    if (isFavorite) R.color.red else R.color.gray
                )
            )
        }
    }
}