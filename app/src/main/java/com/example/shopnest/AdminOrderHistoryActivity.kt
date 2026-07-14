package com.example.shopnest

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.shopnest.adapters.AdminOrderHistoryAdapter
import com.example.shopnest.Model.AdminOrderHistoryItem
import com.example.shopnest.DatabaseHelper

class AdminOrderHistoryActivity : AppCompatActivity() {

    private lateinit var rvOrderHistory: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var tvOrderSummary: TextView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_order_history)

        rvOrderHistory = findViewById(R.id.rvAdminOrderHistory)
        tvEmptyState = findViewById(R.id.tvAdminOrderHistoryEmpty)
        tvOrderSummary = findViewById(R.id.tvOrderSummary)
        dbHelper = DatabaseHelper(this)

        setupRecyclerView()
        loadOrderHistory()

        tvOrderSummary.setOnClickListener {
            showOrderSummaryPopup()
        }

    }

    private fun setupRecyclerView() {
        rvOrderHistory.layoutManager = LinearLayoutManager(this)
        rvOrderHistory.setHasFixedSize(true)
    }
    private fun showOrderSummaryPopup() {
        val orderItems = dbHelper.getAllAdminOrderHistoryItems()
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_order_summary, null)

        // Calculate summary metrics
        val totalOrders = orderItems.size
        val totalProducts = orderItems.sumOf { it.quantity }
        val totalRevenue = orderItems.sumOf { it.totalPrice }
        val totalDiscount = orderItems.sumOf { it.totalPrice * 0.1 } // Assuming 10% discount
        val netRevenue = totalRevenue - totalDiscount

        // Set values to popup views
        popupView.findViewById<TextView>(R.id.tvTotalOrders).text = "📋 Total Orders: $totalOrders"
        popupView.findViewById<TextView>(R.id.tvTotalProducts).text = "🛍️ Products Ordered: $totalProducts"
        popupView.findViewById<TextView>(R.id.tvTotalRevenue).text = "💰 Gross Revenue: ₹${"%.2f".format(totalRevenue)}"
        popupView.findViewById<TextView>(R.id.tvTotalDiscount).text = "🎁 Total Discount: ₹${"%.2f".format(totalDiscount)}"
        popupView.findViewById<TextView>(R.id.tvNetRevenue).text = "💵 Net Revenue: ₹${"%.2f".format(netRevenue)}"

        // Create and show popup
        PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(ContextCompat.getDrawable(this@AdminOrderHistoryActivity, android.R.color.transparent))
            isOutsideTouchable = true
            elevation = 10f
            showAsDropDown(tvOrderSummary, (-tvOrderSummary.width * 1.5).toInt(), 0, Gravity.END)
        }
    }
    private fun loadOrderHistory() {
        val orderItems = dbHelper.getAllAdminOrderHistoryItems()

        if (orderItems.isEmpty()) {
            showEmptyState()
        } else {
            showOrderHistory(orderItems)
        }
    }

    private fun showEmptyState() {
        rvOrderHistory.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
    }

    private fun showOrderHistory(orderItems: List<AdminOrderHistoryItem>) {
        rvOrderHistory.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
        rvOrderHistory.adapter = AdminOrderHistoryAdapter(orderItems)
    }
}