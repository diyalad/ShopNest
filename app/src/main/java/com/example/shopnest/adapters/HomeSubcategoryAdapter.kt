package com.example.shopnest.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopnest.Model.Subcategory
import com.example.shopnest.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.io.File

class HomeSubcategoryAdapter(
    private val subcategoryList: ArrayList<Subcategory>,
    private val onSubcategoryClicked: (Subcategory) -> Unit,
    private val onAddToCartClicked: (Subcategory) -> Unit
) : RecyclerView.Adapter<HomeSubcategoryAdapter.SubcategoryViewHolder>() {

    // Keep track of the currently centered item
    private var centeredItemPosition = -1

    inner class SubcategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView as MaterialCardView
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val ratingText: TextView = itemView.findViewById(R.id.ratingText)
        private val discountedPriceText: TextView = itemView.findViewById(R.id.discountedPriceText)
        private val originalPriceText: TextView = itemView.findViewById(R.id.originalPriceText)
        private val discountText: TextView = itemView.findViewById(R.id.discountedPriceText)
        private val deliveryInfoText: TextView = itemView.findViewById(R.id.discountText)
        private val addToCartButton: MaterialButton = itemView.findViewById(R.id.addToCartButton)

        fun bind(subcategory: Subcategory, isCentered: Boolean) {
            // Load image from file path
            loadImageFromPath(subcategory.imagePath)

            productName.text = subcategory.name
            ratingText.text = "${subcategory.rating} ⭐"
            discountedPriceText.text = formatPrice(subcategory.discountedPrice)
            originalPriceText.text = "M.R.P: ${formatPrice(subcategory.originalPrice)}"
            discountText.text = "${subcategory.discountPercentage}% off"
            deliveryInfoText.text = subcategory.deliveryInfo

            // Apply strikethrough to the original price
            originalPriceText.paintFlags = originalPriceText.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG

            // Apply visual effects based on centered state
            updateCardAppearance(isCentered)

            // Set click listeners
            addToCartButton.setOnClickListener { onAddToCartClicked(subcategory) }
            itemView.setOnClickListener { onSubcategoryClicked(subcategory) }
        }

        private fun loadImageFromPath(imagePath: String?) {
            try {
                if (!imagePath.isNullOrEmpty()) {
                    val file = File(imagePath)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        productImage.setImageBitmap(bitmap)
                        return
                    }
                }
                productImage.setImageResource(R.drawable.ic_category_placeholder)
            } catch (e: Exception) {
                productImage.setImageResource(R.drawable.ic_category_placeholder)
            }
        }

        private fun formatPrice(price: Double): String {
            return if (price % 1 == 0.0) {
                "₹${price.toInt()}"
            } else {
                "₹%.2f".format(price)
            }
        }

        private fun updateCardAppearance(isCentered: Boolean) {
            if (isCentered) {
                val scaleUp = AnimationUtils.loadAnimation(itemView.context, R.anim.scale_up)
                cardView.startAnimation(scaleUp)
                cardView.elevation = 12f
                cardView.strokeWidth = 4
            } else {
                val scaleDown = AnimationUtils.loadAnimation(itemView.context, R.anim.scale_down)
                cardView.startAnimation(scaleDown)
                cardView.elevation = 4f
                cardView.strokeWidth = 2
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubcategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_product, parent, false)
        return SubcategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubcategoryViewHolder, position: Int) {
        holder.bind(subcategoryList[position], position == centeredItemPosition)
    }

    override fun getItemCount(): Int = subcategoryList.size

    fun setCenteredItemPosition(position: Int) {
        if (position != centeredItemPosition) {
            val oldPosition = centeredItemPosition
            centeredItemPosition = position

            // Only update the changed items for better performance
            if (oldPosition in 0 until itemCount) {
                notifyItemChanged(oldPosition)
            }
            if (centeredItemPosition in 0 until itemCount) {
                notifyItemChanged(centeredItemPosition)
            }
        }
    }

    fun updateList(newList: List<Subcategory>) {
        subcategoryList.clear()
        subcategoryList.addAll(newList)
        notifyDataSetChanged()
    }
}