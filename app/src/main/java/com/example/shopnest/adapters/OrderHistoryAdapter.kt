package com.example.shopnest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopnest.databinding.ItemOrderHistoryBinding
import com.example.shopnest.Model.OrderHistory
import com.example.shopnest.R
import java.io.File

class OrderHistoryAdapter(private var orderHistoryList: List<OrderHistory>) :
    RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder>() {

    inner class OrderHistoryViewHolder(val binding: ItemOrderHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val binding = ItemOrderHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        val order = orderHistoryList[position]
        holder.binding.apply {
            // Bind product information
            productName.text = order.productName
            productSize.text = "Size: ${order.size}"
            totalPrice.text = "₹${order.totalPrice}"
            productRating.text = "Rating: ${order.productRating} ★"

            // Bind delivery information
            deliveryAddress.text = order.deliveryAddress
            deliveryDate.text = "Delivery: ${order.deliveryStartDate} - ${order.deliveryEndDate}"
            paymentStatus.text = "Payment Status: ${order.paymentStatus}"

            // Bind order status
            updateOrderStatus(
                orderedRadioButton,
                shippedRadioButton,
                outForDeliveryRadioButton,
                deliveredRadioButton,
                line1,
                line2,
                line3,
                order.paymentStatus
            )

            // Load product image
            loadProductImage(productImage, order.productImagePath)
        }
    }

    private fun loadProductImage(imageView: ImageView, imagePath: String) {
        try {
            when {
                imagePath.startsWith("http://") || imagePath.startsWith("https://") -> {
                    // Load from URL
                    Glide.with(imageView.context)
                        .load(imagePath)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(imageView)
                }
                imagePath.startsWith("content://") || imagePath.startsWith("file://") -> {
                    // Load from content provider or file URI
                    Glide.with(imageView.context)
                        .load(imagePath)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(imageView)
                }
                imagePath.isNotEmpty() -> {
                    // Load from file path
                    val imageFile = File(imagePath)
                    if (imageFile.exists()) {
                        Glide.with(imageView.context)
                            .load(imageFile)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .into(imageView)
                    } else {
                        // If file doesn't exist, show placeholder
                        imageView.setImageResource(R.drawable.placeholder_image)
                    }
                }
                else -> {
                    // No image path provided
                    imageView.setImageResource(R.drawable.placeholder_image)
                }
            }
        } catch (e: Exception) {
            // Handle any exceptions during image loading
            imageView.setImageResource(R.drawable.error_image)
        }
    }

    private fun updateOrderStatus(
        ordered: RadioButton,
        shipped: RadioButton,
        outForDelivery: RadioButton,
        delivered: RadioButton,
        line1: View,
        line2: View,
        line3: View,
        status: String?
    ) {
        // Reset all views first
        ordered.isChecked = false
        shipped.isChecked = false
        outForDelivery.isChecked = false
        delivered.isChecked = false
        line1.setBackgroundColor(ContextCompat.getColor(line1.context, R.color.gray_light))
        line2.setBackgroundColor(ContextCompat.getColor(line2.context, R.color.gray_light))
        line3.setBackgroundColor(ContextCompat.getColor(line3.context, R.color.gray_light))

        when (status?.lowercase()) {
            "ordered" -> {
                ordered.isChecked = true
                line1.setBackgroundColor(ContextCompat.getColor(line1.context, R.color.gray_light))
            }
            "shipped" -> {
                ordered.isChecked = true
                shipped.isChecked = true
                line1.setBackgroundColor(ContextCompat.getColor(line1.context, R.color.deep_blue))
                line2.setBackgroundColor(ContextCompat.getColor(line2.context, R.color.gray_light))
            }
            "out for delivery" -> {
                ordered.isChecked = true
                shipped.isChecked = true
                outForDelivery.isChecked = true
                line1.setBackgroundColor(ContextCompat.getColor(line1.context, R.color.deep_blue))
                line2.setBackgroundColor(ContextCompat.getColor(line2.context, R.color.deep_blue))
                line3.setBackgroundColor(ContextCompat.getColor(line3.context, R.color.gray_light))
            }
            "delivered" -> {
                ordered.isChecked = true
                shipped.isChecked = true
                outForDelivery.isChecked = true
                delivered.isChecked = true
                line1.setBackgroundColor(ContextCompat.getColor(line1.context, R.color.deep_blue))
                line2.setBackgroundColor(ContextCompat.getColor(line2.context, R.color.deep_blue))
                line3.setBackgroundColor(ContextCompat.getColor(line3.context, R.color.deep_blue))
            }
            else -> {
                // Default case for unknown status
                ordered.isChecked = true
                line1.setBackgroundColor(ContextCompat.getColor(line1.context, R.color.gray_light))
            }
        }
    }

    override fun getItemCount(): Int = orderHistoryList.size

    fun updateData(newList: List<OrderHistory>) {
        orderHistoryList = newList
        notifyDataSetChanged()
    }
}