package com.example.shopnest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.shopnest.Model.Subcategory
import com.example.shopnest.adapters.FavoriteProductsAdapter
import com.example.shopnest.databinding.ActivityFavouritesActivitlyBinding

class FavouritesActivitly : AppCompatActivity() {

    private lateinit var binding: ActivityFavouritesActivitlyBinding
    private lateinit var favoriteProductsAdapter: FavoriteProductsAdapter
    private val favoriteProductsList = ArrayList<Subcategory>()
    private var userId: Long = -1
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityFavouritesActivitlyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize dbHelper
        dbHelper = DatabaseHelper(this)

        // Set userId
        userId = getUserIdFromPreferencesOrIntent()

        setupRecyclerView()
        loadFavorites()
        setupEmptyState()
    }

    private fun setupRecyclerView() {
        favoriteProductsAdapter = FavoriteProductsAdapter(
            favoriteProductsList,
            onItemClick = { openProductDetails(it) },
            onBuyNowClick = { openBuyNow(it) },
            onFavoriteClick = { subcategory, isFavorite ->
                handleFavoriteClick(subcategory, isFavorite)
            }
        )

        binding.favoritesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@FavouritesActivitly, 2)
            adapter = favoriteProductsAdapter
        }
    }

    private fun openProductDetails(subcategory: Subcategory) {
        Intent(this, ProductDetailsActivity::class.java).apply {
            putExtra("CATEGORY_ID", subcategory.categoryId)
            putExtra("CATEGORY_NAME", "Category Name")
            putExtra("SUBCATEGORY_ID", subcategory.id)
            putExtra("SUBCATEGORY_NAME", subcategory.name)
            putExtra("SUBCATEGORY_IMAGE", subcategory.imagePath)
            putExtra("SUBCATEGORY_CATEGORY_ID", subcategory.categoryId)
            putExtra("SUBCATEGORY_DISCOUNTED_PRICE", subcategory.discountedPrice)
            putExtra("SUBCATEGORY_ORIGINAL_PRICE", subcategory.originalPrice)
            putExtra("SUBCATEGORY_DISCOUNT_PERCENTAGE", subcategory.discountPercentage)
            putExtra("SUBCATEGORY_DELIVERY_INFO", subcategory.deliveryInfo)
            putExtra("SUBCATEGORY_DETAILS", subcategory.details)
            putExtra("SUBCATEGORY_RATING", subcategory.rating)
            putExtra("USER_ID", userId)
        }.also { startActivity(it) }
    }

    private fun openBuyNow(subcategory: Subcategory) {
        Intent(this, BuyNowActivity::class.java).apply {
            putExtra("CATEGORY_ID", subcategory.categoryId)
            putExtra("CATEGORY_NAME", "Category Name")
            putExtra("SUBCATEGORY_ID", subcategory.id)
            putExtra("SUBCATEGORY_NAME", subcategory.name)
            putExtra("SUBCATEGORY_IMAGE", subcategory.imagePath)
            putExtra("SUBCATEGORY_CATEGORY_ID", subcategory.categoryId)
            putExtra("SUBCATEGORY_DISCOUNTED_PRICE", subcategory.discountedPrice)
            putExtra("SUBCATEGORY_ORIGINAL_PRICE", subcategory.originalPrice)
            putExtra("SUBCATEGORY_DISCOUNT_PERCENTAGE", subcategory.discountPercentage)
            putExtra("SUBCATEGORY_DELIVERY_INFO", subcategory.deliveryInfo)
            putExtra("SUBCATEGORY_DETAILS", subcategory.details)
            putExtra("SUBCATEGORY_RATING", subcategory.rating)
            putExtra("USER_ID", userId)
        }.also { startActivity(it) }
    }

    private fun handleFavoriteClick(subcategory: Subcategory, isFavorite: Boolean) {
        if (!isFavorite) { // Only handle removal case
            dbHelper.removeFavorite(userId, subcategory.id).also { result ->
                if (result > 0) {
                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to remove favorite", Toast.LENGTH_SHORT).show()
                }
            }

            // No need to manually remove from list here - adapter handles it
            // Show empty state if no favorites left
            if (favoriteProductsList.isEmpty()) {
                binding.emptyStateView.visibility = View.VISIBLE
                binding.favoritesRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun loadFavorites() {
        Log.d("FavouritesActivity", "Loading favorites for user $userId")
        val favorites = dbHelper.getFavoritesByUserId(userId)

        favoriteProductsList.clear()
        favoriteProductsList.addAll(favorites)

        if (favorites.isEmpty()) {
            Log.d("FavouritesActivity", "No favorites found")
            binding.emptyStateView.visibility = View.VISIBLE
            binding.favoritesRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateView.visibility = View.GONE
            binding.favoritesRecyclerView.visibility = View.VISIBLE
            favoriteProductsAdapter.notifyDataSetChanged()
        }
    }

    private fun getUserIdFromPreferencesOrIntent(): Long {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getLong("userId", -1)
        Log.d("FavouritesActivitly", "Retrieved userId: $userId")
        return userId
    }

    private fun fetchUserDetails(userId: Long) {
        val user = dbHelper.getUserById(userId)
        if (user != null) {
            Log.d("FavouritesActivitly", "User details: ${user.name}, ${user.email}, ${user.phone}")
        } else {
            Log.e("FavouritesActivitly", "User not found in database")
        }
    }

    private fun setupEmptyState() {
        binding.emptyStateView.apply {
            visibility = if (favoriteProductsList.isEmpty()) View.VISIBLE else View.GONE
            findViewById<Button>(R.id.btnExplore).setOnClickListener {
                startActivity(Intent(this@FavouritesActivitly, SubcategoryProductsActivity::class.java))
                finish()
            }
        }
    }
}