package com.example.shopnest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.shopnest.Model.Subcategory
import com.example.shopnest.adapters.SubcategoryAdapter
import com.example.shopnest.databinding.ActivitySubcategoryProductsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import java.util.Collections

class SubcategoryProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubcategoryProductsBinding
    private lateinit var subcategoryAdapter: SubcategoryAdapter
    private val subcategoryList = ArrayList<Subcategory>()
    private var userId: Long = -1
    private lateinit var dbHelper: DatabaseHelper
    private var isPriceAscending = true
    private var isRatingAscending = true
    private var currentMinPrice = 0f
    private var currentMaxPrice = 10000f
    private var currentMinRating = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubcategoryProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper.getInstance(this)

        // Initialize favorite icon click
        val favorite = findViewById<ImageView>(R.id.favorite)
        favorite.setOnClickListener {
            val i = Intent(this, FavouritesActivitly::class.java)
            i.putExtra("USER_ID", userId)
            startActivity(i)
        }

        binding.cartIcon.setOnClickListener {
            val i = Intent(this, cart::class.java)
            i.putExtra("USER_ID", userId)
            startActivity(i)
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get user ID
        userId = intent.getLongExtra("USER_ID", -1)
        if (userId == -1L) {
            Log.e("SubcategoryProducts", "User ID not found")
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Log.d("SubcategoryProducts", "User ID: $userId")
            fetchUserDetails(userId)
        }

        // Get category info
        val categoryId = intent.getIntExtra("CATEGORY_ID", -1)
        val categoryName = intent.getStringExtra("CATEGORY_NAME")

        // Initialize adapter
        subcategoryAdapter = SubcategoryAdapter(
            subcategoryList,
            { subcategory -> openProductDetails(subcategory, categoryId, categoryName) },
            { subcategory -> openProductDetails(subcategory, categoryId, categoryName) },
            { subcategory, isFavorite -> toggleFavorite(subcategory, isFavorite) },
            { subcategory -> addToCart(subcategory) }
        )

        // Setup RecyclerView
        binding.productsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@SubcategoryProductsActivity, 2)
            adapter = subcategoryAdapter
            setHasFixedSize(true)
        }

        // Set up filter buttons
        setupFilterButtons(categoryId)

        // Load products
        if (categoryId != -1) {
            loadSubcategories(categoryId)
        } else {
            Toast.makeText(this, "Category not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupFilterButtons(categoryId: Int) {
        // Price sort button
        binding.btnSortPrice.setOnClickListener {
            isPriceAscending = !isPriceAscending
            sortByPrice(isPriceAscending)
            updateSortButtonIcon(binding.btnSortPrice, isPriceAscending)
        }

        // Rating sort button
        binding.btnSortRating.setOnClickListener {
            isRatingAscending = !isRatingAscending
            sortByRating(isRatingAscending)
            updateSortButtonIcon(binding.btnSortRating, isRatingAscending)
        }

        // Filter button
        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun sortByPrice(ascending: Boolean) {
        if (ascending) {
            Collections.sort(subcategoryList) { p1, p2 ->
                p1.discountedPrice.compareTo(p2.discountedPrice)
            }
        } else {
            Collections.sort(subcategoryList) { p1, p2 ->
                p2.discountedPrice.compareTo(p1.discountedPrice)
            }
        }
        subcategoryAdapter.notifyDataSetChanged()
    }

    private fun sortByRating(ascending: Boolean) {
        if (ascending) {
            Collections.sort(subcategoryList) { p1, p2 ->
                p1.rating.compareTo(p2.rating)
            }
        } else {
            Collections.sort(subcategoryList) { p1, p2 ->
                p2.rating.compareTo(p1.rating)
            }
        }
        subcategoryAdapter.notifyDataSetChanged()
    }

    private fun updateSortButtonIcon(button: MaterialButton, ascending: Boolean) {
        val iconRes = if (ascending) {
            R.drawable.baseline_arrow_upward_24
        } else {
            R.drawable.baseline_arrow_downward_24
        }
        button.icon = resources.getDrawable(iconRes, theme)
    }

    private fun showFilterDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_filter, null)
        dialog.setContentView(view)

        val priceSlider = view.findViewById<RangeSlider>(R.id.priceSlider)
        val ratingSlider = view.findViewById<Slider>(R.id.ratingSlider) // Changed to Slider
        val btnApply = view.findViewById<MaterialButton>(R.id.btnApplyFilter)
        val btnReset = view.findViewById<MaterialButton>(R.id.btnResetFilter)

        // Set current values
        priceSlider.setValues(currentMinPrice, currentMaxPrice)
        ratingSlider.value = currentMinRating // Set single value for Slider

        btnApply.setOnClickListener {
            currentMinPrice = priceSlider.values[0]
            currentMaxPrice = priceSlider.values[1]
            currentMinRating = ratingSlider.value // Get single value from Slider

            filterProducts(
                minPrice = currentMinPrice.toDouble(),
                maxPrice = currentMaxPrice.toDouble(),
                minRating = currentMinRating
            )
            dialog.dismiss()
        }

        btnReset.setOnClickListener {
            currentMinPrice = 0f
            currentMaxPrice = 10000f
            currentMinRating = 0f
            priceSlider.setValues(0f, 10000f)
            ratingSlider.value = 0f // Reset single value for Slider
            filterProducts()
        }

        dialog.show()
    }

    private fun filterProducts(minPrice: Double? = null, maxPrice: Double? = null, minRating: Float? = null) {
        val filteredList = if (minPrice == null && maxPrice == null && minRating == null) {
            // No filters - show all products
            dbHelper.getSubcategoriesByCategoryId(intent.getIntExtra("CATEGORY_ID", -1))
                .map { it.copy(isFavorite = dbHelper.isFavorite(userId, it.id)) }
        } else {
            subcategoryList.filter { product ->
                (minPrice == null || product.discountedPrice >= minPrice) &&
                        (maxPrice == null || product.discountedPrice <= maxPrice) &&
                        (minRating == null || product.rating >= minRating)
            }
        }

        subcategoryList.clear()
        subcategoryList.addAll(filteredList)
        subcategoryAdapter.notifyDataSetChanged()
    }

    private fun openProductDetails(subcategory: Subcategory, categoryId: Int, categoryName: String?) {
        val intent = Intent(this, ProductDetailsActivity::class.java).apply {
            putExtra("USER_ID", userId)
            putExtra("SUBCATEGORY_ID", subcategory.id)
            putExtra("SUBCATEGORY_NAME", subcategory.name)
            putExtra("SUBCATEGORY_IMAGE", subcategory.imagePath)
            putExtra("CATEGORY_ID", subcategory.categoryId)
            putExtra("SUBCATEGORY_DISCOUNTED_PRICE", subcategory.discountedPrice)
            putExtra("SUBCATEGORY_ORIGINAL_PRICE", subcategory.originalPrice)
            putExtra("SUBCATEGORY_DISCOUNT_PERCENTAGE", subcategory.discountPercentage)
            putExtra("SUBCATEGORY_DELIVERY_INFO", subcategory.deliveryInfo)
            putExtra("SUBCATEGORY_DETAILS", subcategory.details)
            putExtra("SUBCATEGORY_RATING", subcategory.rating)
        }
        startActivity(intent)
    }

    private fun loadSubcategories(categoryId: Int) {
        val subcategories = dbHelper.getSubcategoriesByCategoryId(categoryId)
        if (subcategories.isEmpty()) {
            Toast.makeText(this, "No products found", Toast.LENGTH_SHORT).show()
            return
        }

        val favoriteSubcategories = subcategories.map { subcategory ->
            subcategory.copy(isFavorite = dbHelper.isFavorite(userId, subcategory.id))
        }

        subcategoryList.clear()
        subcategoryList.addAll(favoriteSubcategories)
        subcategoryAdapter.notifyDataSetChanged()

        subcategories.forEach {
            Log.d("ProductData", """
                Product: ${it.name}
                Rating: ${it.rating}
                Discount: ${it.discountPercentage}%
                Price: ${it.discountedPrice} (was ${it.originalPrice})
                Favorite: ${dbHelper.isFavorite(userId, it.id)}
            """.trimIndent())
        }
    }

    private fun toggleFavorite(subcategory: Subcategory, isFavorite: Boolean) {
        if (isFavorite) {
            dbHelper.addFavorite(userId, subcategory.id)
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
        } else {
            dbHelper.removeFavorite(userId, subcategory.id)
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
        }
        subcategory.isFavorite = isFavorite
    }

    private fun addToCart(subcategory: Subcategory) {
        val success = dbHelper.addToCart(userId, subcategory) > 0
        if (success) {
            Toast.makeText(this, "${subcategory.name} added to cart", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserDetails(userId: Long) {
        val user = dbHelper.getUserById(userId)
        if (user != null) {
            Log.d("UserData", "User: ${user.name}, ${user.email}")
        } else {
            Log.e("UserData", "User not found")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}