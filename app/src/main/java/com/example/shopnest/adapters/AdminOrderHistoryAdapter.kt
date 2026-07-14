package com.example.shopnest.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopnest.Model.AdminOrderHistoryItem
import com.example.shopnest.R
import com.google.android.material.card.MaterialCardView
import android.widget.ImageView

class AdminOrderHistoryAdapter(
    private val orderItems: List<AdminOrderHistoryItem>
) : RecyclerView.Adapter<AdminOrderHistoryAdapter.AdminOrderHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminOrderHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_order_history, parent, false)
        return AdminOrderHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminOrderHistoryViewHolder, position: Int) {
        holder.bind(orderItems[position])
    }

    override fun getItemCount(): Int = orderItems.size

    inner class AdminOrderHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardAdminOrderItem)
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivAdminOrderProductImage)
        private val tvOrderId: TextView = itemView.findViewById(R.id.tvAdminOrderId)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvAdminOrderProductName)
        private val tvUserInfo: TextView = itemView.findViewById(R.id.tvAdminOrderUserInfo)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvAdminOrderQuantity)
        private val tvTotalPrice: TextView = itemView.findViewById(R.id.tvAdminOrderTotalPrice)
        private val tvPaymentStatus: TextView = itemView.findViewById(R.id.tvAdminOrderPaymentStatus)
        private val tvDeliveryPeriod: TextView = itemView.findViewById(R.id.tvAdminOrderDeliveryPeriod)
        private val tvOrderDate: TextView = itemView.findViewById(R.id.tvAdminOrderDate)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvAdminOrderAddress)

        fun bind(orderItem: AdminOrderHistoryItem) {
            ivProductImage.setImageResource(orderItem.productImageResId)
            tvOrderId.text = "🆔 Order #${orderItem.orderId}"
            tvProductName.text = orderItem.productName
            tvUserInfo.text = "👤 ${orderItem.userName} (${orderItem.userEmail})"
            tvQuantity.text = "🔢 Quantity: ${orderItem.quantity}"
            tvTotalPrice.text = "💰 Total: ${orderItem.formattedTotalPrice()}"
            tvPaymentStatus.text = when (orderItem.paymentStatus) {
                "paid" -> "💳 Payment: Paid (${orderItem.paymentId})"
                else -> "💳 Payment: ${orderItem.paymentStatus}"
            }
            tvDeliveryPeriod.text = orderItem.formattedDeliveryPeriod()
            tvOrderDate.text = orderItem.formattedOrderDate()
            tvAddress.text = "🏠 ${orderItem.address}"

            // Set card elevation and color based on payment status
            cardView.apply {
                strokeWidth = 2
                strokeColor = when (orderItem.paymentStatus) {
                    "paid" -> context.getColor(R.color.green)
                    "pending" -> context.getColor(R.color.deep_blue)
                    else -> context.getColor(R.color.red)
                }
                cardElevation = 8f
            }
        }
    }
}