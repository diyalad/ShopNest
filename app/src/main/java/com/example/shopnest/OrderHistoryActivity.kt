package com.example.shopnest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopnest.adapters.OrderHistoryAdapter
import com.example.shopnest.databinding.ActivityOrderHistoryBinding

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderHistoryBinding
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the title and back button
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        // Initialize RecyclerView
        setupRecyclerView()

        // Load order history data
        loadOrderHistory()
    }

    private fun setupRecyclerView() {
        orderHistoryAdapter = OrderHistoryAdapter(emptyList())
        binding.orderHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrderHistoryActivity)
            adapter = orderHistoryAdapter
        }
    }

    private fun loadOrderHistory() {
        val dbHelper = DatabaseHelper(this)
        val orderHistoryList = dbHelper.getAllOrderHistoryWithDetails()

        if (orderHistoryList.isEmpty()) {
            // Show empty state
            binding.emptyStateLayout.visibility = android.view.View.VISIBLE
            binding.orderHistoryRecyclerView.visibility = android.view.View.GONE
        } else {
            // Hide empty state and show data
            binding.emptyStateLayout.visibility = android.view.View.GONE
            binding.orderHistoryRecyclerView.visibility = android.view.View.VISIBLE

            // Update adapter with new data
            orderHistoryAdapter.updateData(orderHistoryList)
        }
    }
}