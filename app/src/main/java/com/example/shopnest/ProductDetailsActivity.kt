package com.example.shopnest

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.shopnest.Model.Subcategory
import com.example.shopnest.adapters.ProductImagesAdapter
import com.example.shopnest.adapters.SimilarProductsAdapter
import com.example.shopnest.databinding.ActivityProductDetailsBinding
import com.google.android.material.tabs.TabLayoutMediator
import java.io.File

class ProductDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailsBinding
    private var selectedSize: String = ""
    private var userId: Long = -1
    private lateinit var dbHelper: DatabaseHelper
    private var subcategory: Subcategory? = null
    private lateinit var viewPager: ViewPager2
    private lateinit var imageAdapter: ProductImagesAdapter
    private lateinit var similarProductsAdapter: SimilarProductsAdapter

    companion object {
        private const val TAG = "ProductDetails"
        private const val IMAGE_TAG = "ImageDebug"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "Activity created")

        // Initialize DatabaseHelper
        dbHelper = DatabaseHelper.getInstance(this)
        Log.d(TAG, "DatabaseHelper initialized")

        // Set up the Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Get user ID
        userId = intent.getLongExtra("USER_ID", -1)
        if (userId == -1L) {
            Log.e(TAG, "User ID not found in intent")
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, signin::class.java))
            finish()
            return
        }
        Log.d(TAG, "User ID retrieved: $userId")

        // Get product ID from intent
        val subcategoryId = intent.getIntExtra("SUBCATEGORY_ID", -1)
        if (subcategoryId == -1) {
            Log.e(TAG, "Subcategory ID not found in intent")
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Fetch product details from the database
        subcategory = fetchSubcategoryFromDatabase(subcategoryId)
        if (subcategory == null) {
            Log.e(TAG, "Failed to fetch product details from database")
            Toast.makeText(this, "Failed to load product details", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        Log.d(TAG, "Product loaded: ${subcategory?.name} (ID: ${subcategory?.id})")

        // Initialize ViewPager with product images
        setupImageSlider()

        // Display product details
        displayProductDetails()

        setupSizeSelection()

        // Set up click listeners
        setupClickListeners()

        // Setup similar products
        setupSimilarProducts()
    }

    private fun fetchSubcategoryFromDatabase(subcategoryId: Int): Subcategory? {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT * FROM ${DatabaseHelper.TABLE_SUBCATEGORIES}
            WHERE ${DatabaseHelper.COLUMN_SUBCATEGORY_ID} = ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(subcategoryId.toString()))

        var subcategory: Subcategory? = null
        if (cursor.moveToFirst()) {
            subcategory = Subcategory(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBCATEGORY_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBCATEGORY_NAME)),
                imagePath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBCATEGORY_IMAGE_PATH)),
                categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_FOREIGN_ID)),
                discountedPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBCATEGORY_DISCOUNTED_PRICE)),
                originalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBCATEGORY_ORIGINAL_PRICE)),
                discountPercentage = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBCATEGORY_DISCOUNT_PERCENTAGE)),
                deliveryInfo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBCATEGORY_DELIVERY_INFO)),
                details = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBCATEGORY_DETAILS)),
                rating = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBCATEGORY_RATING)),
                isFavorite = dbHelper.isFavorite(userId, subcategoryId)
            )
        }
        cursor.close()
        return subcategory
    }

    private fun setupImageSlider() {
        Log.d(IMAGE_TAG, "Initializing image slider")

        // Validate the image path before using it
        val validImagePath = subcategory?.imagePath?.let {
            if (File(it).exists()) it else null
        } ?: run {
            Log.w(IMAGE_TAG, "Main product image not found, using placeholder")
            null
        }

        // Create list with validated image paths
        val imagePaths = listOfNotNull(validImagePath, validImagePath, validImagePath)

        // Log image paths for debugging
        logImagePaths(imagePaths)

        viewPager = binding.productImagePager
        imageAdapter = ProductImagesAdapter(imagePaths).apply {
            setImageLoadListener(object : ProductImagesAdapter.ImageLoadListener {
                override fun onImageLoadSuccess(position: Int, imagePath: String?) {
                    Log.d(IMAGE_TAG, "✅ Successfully loaded image at position $position")
                }

                override fun onImageLoadFailure(position: Int, imagePath: String?, error: String) {
                    Log.e(IMAGE_TAG, "❌ Failed to load image at position $position: $error")
                }
            })
        }
        viewPager.adapter = imageAdapter
        TabLayoutMediator(binding.imageIndicator, viewPager) { _, _ -> }.attach()
    }

    private fun logImagePaths(paths: List<String>) {
        paths.forEachIndexed { index, path ->
            Log.d(IMAGE_TAG, "🖼️ Image $index path: $path")
            logImageDetails(path)
        }
    }

    private fun logImageDetails(path: String) {
        try {
            val file = File(path)
            Log.d(IMAGE_TAG, "   ├── Exists: ${file.exists()}")
            Log.d(IMAGE_TAG, "   ├── Readable: ${file.canRead()}")
            Log.d(IMAGE_TAG, "   ├── Size: ${if (file.exists()) "${file.length()} bytes" else "N/A"}")
            Log.d(IMAGE_TAG, "   └── Path validity: ${file.absolutePath == path}")
        } catch (e: Exception) {
            Log.e(IMAGE_TAG, "   └── Error checking file: ${e.message}")
        }
    }

    private fun displayProductDetails() {
        Log.d(TAG, "Displaying product details")

        subcategory?.let {
            // Log main product image
            Log.d(IMAGE_TAG, "Main product image:")
            logImageDetails(it.imagePath)

            // Basic product info
            binding.productName.text = it.name
            binding.discountPrice.text = "₹${it.discountedPrice.toInt()}".also { price ->
                Log.d(TAG, "Displaying price: $price")
            }
            binding.originalPrice.text = "₹${it.originalPrice.toInt()}".also { price ->
                Log.d(TAG, "Displaying original price: $price")
            }
            binding.originalPrice.paintFlags = binding.originalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            binding.discountText.text = "${it.discountPercentage}% off".also { discount ->
                Log.d(TAG, "Displaying discount: $discount")
            }

            // Rating and reviews
            val reviewCount = getRandomReviewCount()
            binding.ratingText.text = "${it.rating} ★ ($reviewCount)".also { rating ->
                Log.d(TAG, "Displaying rating: $rating")
            }

            // Delivery info
            binding.deliveryText.text = it.deliveryInfo.also { deliveryInfo ->
                Log.d(TAG, "Displaying delivery info: $deliveryInfo")
            }

            // Product details
            binding.productDetails.text = it.details.also { details ->
                Log.d(TAG, "Displaying product details (length: ${details.length})")
            }

            // Favorite icon
            updateFavoriteIcon(it.isFavorite)
        }
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        Log.d(TAG, "Updating favorite icon: ${if (isFavorite) "filled" else "outlined"}")
        binding.favorite.setImageResource(
            if (isFavorite) R.drawable.baseline_favorite_24
            else R.drawable.baseline_favorite_border_24
        )
    }

    private fun getRandomReviewCount(): String {
        return "${(1000..50000).random()}".also {
            Log.d(TAG, "Generated random review count: $it")
        }
    }

    private fun setupSizeSelection() {
        subcategory?.let { product ->
            if (product.categoryId == 1) {  // Fashion category
                Log.d(TAG, "Setting up size selection for fashion category")
                binding.sizeChipGroup.visibility = View.VISIBLE

                // Initialize with no selection
                selectedSize = ""

                binding.sizeChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
                    selectedSize = when (checkedIds.firstOrNull()) {
                        R.id.sizeSmall -> "S".also { Log.d(TAG, "Size selected: S") }
                        R.id.sizeMedium -> "M".also { Log.d(TAG, "Size selected: M") }
                        R.id.sizeLarge -> "L".also { Log.d(TAG, "Size selected: L") }
                        R.id.sizeXL -> "XL".also { Log.d(TAG, "Size selected: XL") }
                        R.id.sizeXXL -> "XXL".also { Log.d(TAG, "Size selected: XXL") }
                        else -> "".also { Log.d(TAG, "No size selected") }
                    }
                }
            } else {
                Log.d(TAG, "Hiding size selection for non-fashion category")
                binding.sizeChipGroup.visibility = View.GONE
                selectedSize = "" // Reset size for non-fashion items
            }
        }
    }

    private fun setupClickListeners() {
        binding.cartIcon.setOnClickListener {
            Log.d(TAG, "Cart icon clicked")
            navigateToCart()
        }

        binding.favorite.setOnClickListener {
            subcategory?.let {
                val newFavoriteState = !it.isFavorite
                Log.d(TAG, "Favorite clicked. New state: $newFavoriteState")

                it.isFavorite = newFavoriteState

                if (newFavoriteState) {
                    dbHelper.addFavorite(userId, it.id).also { result ->
                        Log.d(TAG, "Added to favorites. Result: $result")
                    }
                } else {
                    dbHelper.removeFavorite(userId, it.id).also { result ->
                        Log.d(TAG, "Removed from favorites. Result: $result")
                    }
                }

                updateFavoriteIcon(newFavoriteState)
                Toast.makeText(this,
                    if (newFavoriteState) "Added to favorites" else "Removed from favorites",
                    Toast.LENGTH_SHORT).show()
            }
        }

        binding.addToBagButton.setOnClickListener {
            Log.d(TAG, "Add to bag clicked")
            if (!validateSizeSelection()) return@setOnClickListener

            subcategory?.let {
                val result = dbHelper.addToCart(userId, it)
                Log.d(TAG, "Add to cart result: $result")

                if (result != -1L) {
                    Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buyNowButton.setOnClickListener {
            Log.d(TAG, "Buy now clicked")
            if (!validateSizeSelection()) return@setOnClickListener
            navigateToBuyNow()
        }

        binding.shareFab.setOnClickListener {
            Log.d(TAG, "Share button clicked")
            shareProduct()
        }
    }

    private fun shareProduct() {
        Log.d(TAG, "Sharing product")
        subcategory?.let {
            val shareMessage = """
                Check out this amazing product: ${it.name}!
                Price: ₹${it.discountedPrice.toInt()} (${it.discountPercentage}% off)
                Original Price: ₹${it.originalPrice.toInt()}
                ${it.details}
                Rating: ${it.rating} ★
            """.trimIndent()

            Log.d(TAG, "Share message: $shareMessage")

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Check out this product!")
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }

            startActivity(Intent.createChooser(shareIntent, "Share product via")).also {
                Log.d(TAG, "Share intent launched")
            }
        }
    }

    private fun validateSizeSelection(): Boolean {
        subcategory?.let { product ->
            if (product.categoryId == 1) {  // Only validate for fashion category
                if (selectedSize.isEmpty()) {
                    Log.d(TAG, "Size validation failed - no size selected")
                    Toast.makeText(this, "Please select a size", Toast.LENGTH_SHORT).show()
                    return false
                }
                Log.d(TAG, "Size validation passed with size: $selectedSize")
            }
        }
        return true
    }
    private fun navigateToCart() {
        Log.d(TAG, "Navigating to cart")
        startActivity(Intent(this, cart::class.java).apply {
            putExtra("USER_ID", userId)
        })
    }

    private fun navigateToBuyNow() {
        Log.d(TAG, "Navigating to buy now")
        subcategory?.let {
            Intent(this, BuyNowActivity::class.java).apply {
                putExtra("USER_ID", userId)
                putExtra("PRODUCT_ID", it.id)
                putExtra("PRODUCT_NAME", it.name)
                putExtra("PRODUCT_IMAGE_PATH", it.imagePath)
                putExtra("PRODUCT_PRICE", it.discountedPrice)
                putExtra("PRODUCT_ORIGINAL_PRICE", it.originalPrice)
                putExtra("PRODUCT_DISCOUNT", it.discountPercentage)
                putExtra("PRODUCT_DELIVERY_INFO", it.deliveryInfo)
                putExtra("PRODUCT_DETAILS", it.details)
                putExtra("PRODUCT_RATING", it.rating)
                putExtra("SELECTED_SIZE", selectedSize)
            }.also { startActivity(it) }
        }
    }

    private fun setupSimilarProducts() {
        Log.d(TAG, "Setting up similar products")

        subcategory?.let {
            val similarProducts = dbHelper.getSubcategoriesByCategory(it.categoryId)
                .filter { product -> product.id != it.id }
                .take(5)

            // Log similar products details
            similarProducts.forEach { product ->
                Log.d(IMAGE_TAG, "Similar product '${product.name}':")
                logImageDetails(product.imagePath)
                Log.d(TAG, "Similar Product ID: ${product.id}")
                Log.d(TAG, "Similar Product Name: ${product.name}")
                Log.d(TAG, "Similar Product Image Path: ${product.imagePath}")
                Log.d(TAG, "Similar Product Category ID: ${product.categoryId}")
                Log.d(TAG, "Similar Product Discounted Price: ${product.discountedPrice}")
                Log.d(TAG, "Similar Product Original Price: ${product.originalPrice}")
                Log.d(TAG, "Similar Product Discount Percentage: ${product.discountPercentage}")
                Log.d(TAG, "Similar Product Delivery Info: ${product.deliveryInfo}")
                Log.d(TAG, "Similar Product Details: ${product.details}")
                Log.d(TAG, "Similar Product Rating: ${product.rating}")
            }

            if (similarProducts.isEmpty()) {
                Log.d(TAG, "No similar products found")
                binding.similarProductsTitle.visibility = View.GONE
                binding.similarProductsRecyclerView.visibility = View.GONE
                return
            }

            similarProductsAdapter = SimilarProductsAdapter(
                products = similarProducts,
                onItemClick = { product ->
                    Log.d(TAG, "Similar product clicked: ${product.name}")
                    Log.d(IMAGE_TAG, "Navigating to product with image: ${product.imagePath}")
                    Intent(this, ProductDetailsActivity::class.java).apply {
                        putExtra("USER_ID", userId)
                        putExtra("SUBCATEGORY_ID", product.id)
                        putExtra("SUBCATEGORY_NAME", product.name)
                        putExtra("SUBCATEGORY_IMAGE_PATH", product.imagePath)
                        putExtra("CATEGORY_ID", product.categoryId)
                        putExtra("SUBCATEGORY_DISCOUNTED_PRICE", product.discountedPrice)
                        putExtra("SUBCATEGORY_ORIGINAL_PRICE", product.originalPrice)
                        putExtra("SUBCATEGORY_DISCOUNT_PERCENTAGE", product.discountPercentage)
                        putExtra("SUBCATEGORY_DELIVERY_INFO", product.deliveryInfo)
                        putExtra("SUBCATEGORY_DETAILS", product.details)
                        putExtra("SUBCATEGORY_RATING", product.rating)
                    }.also { startActivity(it) }
                },
                onFavoriteClick = { product, isFavorite ->
                    Log.d(TAG, "Similar product favorite clicked: ${product.name} ($isFavorite)")
                    if (isFavorite) {
                        dbHelper.addFavorite(userId, product.id).also {
                            Log.d(TAG, "Added similar to favorites. Result: $it")
                        }
                    } else {
                        dbHelper.removeFavorite(userId, product.id).also {
                            Log.d(TAG, "Removed similar from favorites. Result: $it")
                        }
                    }
                    Toast.makeText(
                        this,
                        if (isFavorite) "Added to favorites" else "Removed from favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onAddToCart = { product ->
                    Log.d(TAG, "Adding similar product to cart: ${product.name}")
                    val result = dbHelper.addToCart(userId, product)
                    Log.d(TAG, "Add to cart result: $result")
                    if (result != -1L) {
                        Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show()
                    }
                }
            ).apply {
                setImageLoadListener(object : SimilarProductsAdapter.ImageLoadListener {
                    override fun onImageLoadSuccess(position: Int, product: Subcategory) {
                        Log.d(IMAGE_TAG, "✅ Successfully loaded similar product image at $position: ${product.name}")
                    }

                    override fun onImageLoadFailure(position: Int, product: Subcategory, error: String) {
                        Log.e(IMAGE_TAG, "❌ Failed to load similar product image at $position: ${product.name} - $error")
                    }
                })
            }

            binding.similarProductsRecyclerView.apply {
                adapter = similarProductsAdapter
                layoutManager = LinearLayoutManager(
                    this@ProductDetailsActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            }
            Log.d(TAG, "Similar products adapter setup completed")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d(TAG, "Back button pressed")
        onBackPressed()
        return true
    }
}
