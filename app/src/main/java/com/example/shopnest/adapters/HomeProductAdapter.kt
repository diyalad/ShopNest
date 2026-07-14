package com.example.shopnest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopnest.Model.Product
import com.example.shopnest.R

class HomeProductAdapter(
    private var productList: List<Product>,
    private val onProductClicked: (Product) -> Unit,
    private val onAddToCartClicked: (Product) -> Unit
) : RecyclerView.Adapter<HomeProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val discountedPriceText: TextView = itemView.findViewById(R.id.discountedPriceText)
        val originalPriceText: TextView = itemView.findViewById(R.id.originalPriceText)
        val discountText: TextView = itemView.findViewById(R.id.discountText)

        fun bind(product: Product) {
            productName.text = product.name
            productImage.setImageResource(product.imageResId)
            discountedPriceText.text = "₹${product.discountedPrice}"
            originalPriceText.text = "M.R.P: ₹${product.originalPrice}"
            discountText.text = "${product.discountPercentage}% off"

            // Apply strikethrough to the original price
            originalPriceText.paintFlags = originalPriceText.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG

            // Handle product item click
            itemView.setOnClickListener {
                onProductClicked(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = productList.size

    // Function to update the product list if needed
    fun updateProductList(newProducts: List<Product>) {
        productList = newProducts
        notifyDataSetChanged()
    }
}