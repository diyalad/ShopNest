package com.example.shopnest

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopnest.Model.AdminFavorite
import com.example.shopnest.adapters.AdminFavoritesAdapter
import com.example.shopnest.databinding.ActivityAdminFavoritesBinding

class AdminFavoritesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminFavoritesBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: AdminFavoritesAdapter
    private var favoritesList = mutableListOf<AdminFavorite>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        setupRecyclerView()
        loadFavorites()
        setupSummaryView()

        binding.backButton.setOnClickListener { onBackPressed() }
    }

    private fun setupSummaryView() {
        binding.summaryTextView.setOnClickListener {
            if (favoritesList.isEmpty()) {
                binding.summaryView.summaryContent.text = "No favorites yet!"
                binding.summaryView.root.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Calculate dynamic data from favoritesList
            val totalFavorites = favoritesList.size
            val totalValue = favoritesList.sumOf { it.subcategory.originalPrice }
            val avgDiscount = favoritesList.map { it.subcategory.discountPercentage }.average().toInt()
            val topRating = favoritesList.maxOf { it.subcategory.rating }

            // Count free vs paid deliveries
            val freeDeliveries = favoritesList.count {
                it.subcategory.deliveryInfo.contains("Free", ignoreCase = true)
            }
            val paidDeliveries = totalFavorites - freeDeliveries

            // Get most popular category
            val popularCategory = favoritesList
                .groupBy { it.subcategory.categoryId }
                .maxByOrNull { it.value.size }
                ?.let {
                    when(it.key) {
                        1 -> "Electronics"
                        2 -> "Fashion"
                        3 -> "Home"
                        else -> "Various"
                    }
                } ?: "Various"

            val summaryText = """
            🔹 Total Favorites: $totalFavorites
            🔸 Total Value: ₹${"%.2f".format(totalValue)}
            🔹 Avg Discount: ${avgDiscount}%
            🔸 Popular Category: $popularCategory
            🔹 Potential Revenue: ₹${"%.2f".format(totalValue * 1.3)}
            🔸 Top Rated: ${"%.1f".format(topRating)}★
            🔹 Delivery Types: Free ($freeDeliveries), Paid ($paidDeliveries)
            🔸 Recommendation: ${getRecommendation(avgDiscount)}
        """.trimIndent()

            binding.summaryView.summaryContent.text = summaryText
            binding.summaryView.root.visibility = View.VISIBLE
        }

        binding.summaryView.closeButton.setOnClickListener {
            binding.summaryView.root.visibility = View.GONE
        }
    }

    private fun getRecommendation(avgDiscount: Int): String {
        return when {
            avgDiscount > 30 -> "Reduce discounts to increase profits"
            avgDiscount < 15 -> "Increase discounts to attract more customers"
            favoritesList.size < 10 -> "Add more products to favorites section"
            else -> "Promote seasonal offers"
        }
    }
    // ... [rest of your helper functions remain the same]

    private fun setupRecyclerView() {
        adapter = AdminFavoritesAdapter(favoritesList)
        binding.favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.favoritesRecyclerView.adapter = adapter
    }

    private fun loadFavorites() {
        val favorites = dbHelper.adminGetAllFavoritesWithDetails()
        Log.d("AdminFavorites", "Loaded ${favorites.size} favorites")
        favorites.forEach { Log.d("AdminFavorite", it.toString()) }

        favoritesList.clear()
        favoritesList.addAll(favorites)
        adapter.notifyDataSetChanged()
    }
    // Extension functions for random ranges
    private fun ClosedRange<Int>.random() = (Math.random() * (endInclusive - start) + start).toInt()
    private fun ClosedFloatingPointRange<Double>.random() = Math.random() * (endInclusive - start) + start
}