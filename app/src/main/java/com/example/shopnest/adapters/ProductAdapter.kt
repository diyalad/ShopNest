package com.example.shopnest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopnest.Model.Product
import com.example.shopnest.R

class ProductAdapter(
    private var productList: List<Product>, // List of products to display
    private val onProductClicked: (Product) -> Unit, // Callback for product item click
    private val onAddToCartClicked: (Product) -> Unit // Callback for "Add to Cart" button click
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // ViewHolder class to hold the views for each item
    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
       // private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val discountedPriceText: TextView = itemView.findViewById(R.id.discountedPriceText)
        private val originalPriceText: TextView = itemView.findViewById(R.id.originalPriceText)
        private val discountText: TextView = itemView.findViewById(R.id.discountText)
        private val addToCartButton: Button = itemView.findViewById(R.id.addToCartButton)

        fun bind(product: Product) {
            productName.text = product.name
            productImage.setImageResource(product.imageResId)
            //ratingBar.rating = product.rating
            discountedPriceText.text = "₹${product.discountedPrice}"
            originalPriceText.text = "M.R.P: ₹${product.originalPrice}"
            discountText.text = "${product.discountPercentage}% off"

            // Apply strikethrough to the original price
            originalPriceText.paintFlags = originalPriceText.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG

            // Handle "Add to Cart" button click
            addToCartButton.setOnClickListener {
                onAddToCartClicked(product)
            }

            // Handle product item click
            itemView.setOnClickListener {
                onProductClicked(product)
            }
        }
    }

    // Inflate the item layout and create the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    // Bind data to the ViewHolder at the specified position
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product)
    }

    // Return the size of the product list
    override fun getItemCount(): Int = productList.size

    // Method to filter the product list
    fun filterList(filteredList: List<Product>) {
        productList = filteredList
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }
}