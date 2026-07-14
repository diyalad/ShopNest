package com.example.shopnest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopnest.Model.CartItem
import com.example.shopnest.R
import java.io.File

class CartAdapter(
    private val cartItems: MutableList<CartItem>,
    private val onItemRemoved: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val productQuantity: TextView = itemView.findViewById(R.id.productQuantity)
        private val removeButton: ImageButton = itemView.findViewById(R.id.removeButton)

        fun bind(cartItem: CartItem) {
            // Load image using Glide
            val imageFile = File(cartItem.imagePath)
            if (imageFile.exists()) {
                Glide.with(itemView.context)
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.ic_category_placeholder)
                    .into(productImage)
            } else {
                productImage.setImageResource(R.drawable.ic_category_placeholder)
            }

            productName.text = cartItem.name
            productQuantity.text = "Qty: ${cartItem.quantity}"

            val discountedPrice = cartItem.getDiscountedPrice() * cartItem.quantity
            val originalPrice = cartItem.price * cartItem.quantity

            productPrice.text = if (cartItem.discount > 0) {
                "₹${discountedPrice.toInt()} (${cartItem.discount.toInt()}% off)"
            } else {
                "₹${originalPrice.toInt()}"
            }

            removeButton.setOnClickListener { onItemRemoved(cartItem) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateItems(newItems: List<CartItem>) {
        cartItems.clear()
        cartItems.addAll(newItems)
        notifyDataSetChanged()
    }
}