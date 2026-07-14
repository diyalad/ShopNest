package com.example.shopnest

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopnest.Model.Category
import com.example.shopnest.adapters.CategoryAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class CategoryActivity : AppCompatActivity() {

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoryList: ArrayList<Category>
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var toolbar: Toolbar
    private var userId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        // Initialize views
        initViews()

        // Get userId from intent
        userId = intent.getLongExtra("USER_ID", -1)

        // Setup toolbar
        setupToolbar()

        // Setup RecyclerView
        setupRecyclerView()

        // Check and insert sample data
        checkAndInsertSampleData()

        // Setup bottom navigation
        setupBottomNavigation()
    }

    private fun initViews() {
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView)
        toolbar = findViewById(R.id.toolbar)
        dbHelper = DatabaseHelper(this)
        categoryList = ArrayList()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "ShopNest Categories"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerView() {
        categoryRecyclerView.layoutManager = GridLayoutManager(this, 2)
        categoryAdapter = CategoryAdapter(categoryList) { category ->
            navigateToSubcategory(category)
        }
        categoryRecyclerView.adapter = categoryAdapter
        loadCategories()
    }

    private fun loadCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            val categories = dbHelper.getAllCategories()

            // Log all categories with their details
            categories.forEach { category ->
                Log.d("CategoryDebug", """
                Category Details:
                ID: ${category.id}
                Name: ${category.name}
                Image Path: ${category.imagePath}
                File Exists: ${File(category.imagePath).exists()}
                ----------------------------
            """.trimIndent())
            }

            withContext(Dispatchers.Main) {
                categoryList.clear()
                categoryList.addAll(categories)
                categoryAdapter.notifyDataSetChanged()

                // Log count of loaded categories
                Log.d("CategoryDebug", "Total categories loaded: ${categoryList.size}")
            }
        }
    }

    private fun checkAndInsertSampleData() {
        CoroutineScope(Dispatchers.IO).launch {
            if (dbHelper.getAllCategories().isEmpty()) {
                Log.d("CategoryActivity", "Inserting sample data...")
                insertAllSampleData()
                withContext(Dispatchers.Main) {
                    loadCategories()
                }
            }
        }
    }

    private fun insertAllSampleData() {
        // Main Categories
        val fashionId = insertCategory("Fashion", R.drawable.fasion)
        val appliancesId = insertCategory("Appliances", R.drawable.appliances)
        val mobilesId = insertCategory("Mobiles", R.drawable.mobiles)
        val electronicsId = insertCategory("Electronics", R.drawable.electronics)
        val homeId = insertCategory("Home", R.drawable.homecategory)
        val beautyId = insertCategory("Beauty", R.drawable.beautyproducts)
        val toysId = insertCategory("Toys", R.drawable.toy2)
        val sportsId = insertCategory("Sports", R.drawable.sporthub)

        // Subcategories
        insertFashionSubcategories(fashionId)
        insertApplianceSubcategories(appliancesId)
        insertMobileSubcategories(mobilesId)
        insertElectronicSubcategories(electronicsId)
        insertHomeSubcategories(homeId)
        insertBeautySubcategories(beautyId)
        insertToySubcategories(toysId)
        insertSportsSubcategories(sportsId)
    }

    private fun insertCategory(name: String, drawableResId: Int): Long {
        val imagePath = saveDrawableToFile(drawableResId).also { path ->
            if (path.isEmpty()) {
                Log.e("CategoryDebug", "❌ Failed to save image for category: $name")
            } else {
                Log.d("CategoryDebug", """
                ✅ Successfully saved image for category:
                Name: $name
                Drawable ID: $drawableResId
                Saved Path: $path
                File Exists: ${File(path).exists()}
            """.trimIndent())
            }
        }

        return dbHelper.insertCategory(name, imagePath).also { id ->
            if (id == -1L) {
                Log.e("CategoryDebug", "❌ Failed to insert category to database: $name")
            } else {
                Log.d("CategoryDebug", """
                🎉 Successfully inserted category to database:
                ID: $id
                Name: $name
                Image Path: $imagePath
            """.trimIndent())
            }
        }
    }

    private fun saveDrawableToFile(drawableResId: Int): String {
        return try {
            val bitmap = BitmapFactory.decodeResource(resources, drawableResId)
            if (bitmap == null) {
                Log.e("CategoryDebug", "❌ Failed to decode bitmap for drawable ID: $drawableResId")
                return ""
            }

            val fileName = "category_${drawableResId}_${System.currentTimeMillis()}.jpg"
            val file = File(cacheDir, fileName)

            FileOutputStream(file).use { stream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)) {
                    Log.e("CategoryDebug", "❌ Failed to compress bitmap for drawable ID: $drawableResId")
                    return ""
                }
            }

            if (!file.exists()) {
                Log.e("CategoryDebug", "❌ File was not created: ${file.absolutePath}")
                return ""
            }

            Log.d("CategoryDebug", """
            💾 Successfully saved drawable to file:
            Drawable ID: $drawableResId
            File Path: ${file.absolutePath}
            File Size: ${file.length()} bytes
        """.trimIndent())

            file.absolutePath
        } catch (e: Exception) {
            Log.e("CategoryDebug", "❌ Error saving drawable ID $drawableResId to file", e)
            ""
        }
    }

    private fun insertSubcategory(
        categoryId: Long,
        name: String,
        imageResId: Int,
        discountedPrice: Double,
        originalPrice: Double,
        discountPercentage: Int,
        deliveryInfo: String,
        details: String,
        rating: Float
    ) {
        val imagePath = saveDrawableToFile(imageResId)
        dbHelper.insertSubcategory(
            name,
            imagePath,
            categoryId.toInt(),
            discountedPrice,
            originalPrice,
            discountPercentage,
            deliveryInfo,
            details,
            rating
        )
    }

    // Region: Subcategory Insertion Methods
    private fun insertFashionSubcategories(categoryId: Long) {
        insertSubcategory(categoryId, "T-Shirts", R.drawable.tshirtandpolos, 999.0, 1999.0, 50,
            "Free delivery in 3 days", "Comfortable cotton t-shirts", 4.3f)
        insertSubcategory(categoryId, "Jeans", R.drawable.jeansmen, 1999.0, 3999.0, 50,
            "Free delivery in 2 days", "Premium quality denim jeans", 4.5f)
        insertSubcategory(categoryId, "Formal Shirts", R.drawable.formalwear, 1499.0, 2999.0, 50,
            "Free delivery in 4 days", "Office wear formal shirts", 4.4f)
        insertSubcategory(categoryId, "Sarees", R.drawable.sari, 2999.0, 5999.0, 50,
            "Free delivery in 5 days", "Traditional Indian sarees", 4.7f)
        insertSubcategory(categoryId, "Kurtas", R.drawable.kurti, 1299.0, 2599.0, 50,
            "Free delivery in 3 days", "Ethnic wear kurtas", 4.6f)
    }

    private fun insertApplianceSubcategories(categoryId: Long) {
        insertSubcategory(categoryId, "Televisions", R.drawable.television, 29999.0, 39999.0, 25,
            "Free delivery in 5 days", "Smart LED TVs", 4.7f)
        insertSubcategory(categoryId, "Refrigerators", R.drawable.img_10, 24999.0, 39999.0, 37,
            "Free delivery in 7 days", "Energy efficient refrigerators", 4.6f)
        insertSubcategory(categoryId, "Washing Machines", R.drawable.washingmachine, 19999.0, 29999.0, 33,
            "Free delivery in 4 days", "Fully automatic washing machines", 4.5f)
        insertSubcategory(categoryId, "Air Conditioners", R.drawable.aircondition, 24999.0, 34999.0, 30,
            "Free delivery in 5 days", "Inverter split ACs", 4.8f)
        insertSubcategory(categoryId, "Microwaves", R.drawable.img_8, 9999.0, 19999.0, 50,
            "Free delivery in 3 days", "Convection microwave ovens", 4.4f)
    }

    private fun insertMobileSubcategories(categoryId: Long) {
        insertSubcategory(categoryId, "Smartphones", R.drawable.iphone, 59999.0, 79999.0, 25,
            "Free delivery in 2 days", "Latest smartphone models", 4.8f)
        insertSubcategory(categoryId, "Tablets", R.drawable.img_15, 14999.0, 24999.0, 40,
            "Free delivery in 3 days", "Powerful tablets", 4.5f)
        insertSubcategory(categoryId, "Smart Watches", R.drawable.img_16, 9999.0, 19999.0, 50,
            "Free delivery in 2 days", "Fitness tracking smartwatches", 4.6f)
        insertSubcategory(categoryId, "Earphones", R.drawable.img_19, 2999.0, 5999.0, 50,
            "Free delivery in 1 day", "Wireless Bluetooth earphones", 4.4f)
        insertSubcategory(categoryId, "Power Banks", R.drawable.img_17, 1999.0, 3999.0, 50,
            "Free delivery in 2 days", "High capacity power banks", 4.3f)
    }

    private fun insertElectronicSubcategories(categoryId: Long) {
        insertSubcategory(categoryId, "Laptops", R.drawable.img_13, 39999.0, 59999.0, 33,
            "Free delivery in 3 days", "High performance laptops", 4.7f)
        insertSubcategory(categoryId, "Printers", R.drawable.img_22, 5999.0, 11999.0, 50,
            "Free delivery in 2 days", "Wireless printers", 4.5f)
        insertSubcategory(categoryId, "Monitors", R.drawable.img_21, 14999.0, 24999.0, 40,
            "Free delivery in 3 days", "HD computer monitors", 4.6f)
        insertSubcategory(categoryId, "Speakers", R.drawable.img_20, 4999.0, 9999.0, 50,
            "Free delivery in 2 days", "Bluetooth speakers", 4.4f)
        insertSubcategory(categoryId, "Cameras", R.drawable.img_18, 29999.0, 49999.0, 40,
            "Free delivery in 4 days", "DSLR cameras", 4.8f)
    }

    private fun insertHomeSubcategories(categoryId: Long) {
        insertSubcategory(categoryId, "Sofas", R.drawable.img_23, 24999.0, 39999.0, 37,
            "Free delivery in 5 days", "Comfortable living room sofas", 4.7f)
        insertSubcategory(categoryId, "Beds", R.drawable.img_26, 19999.0, 39999.0, 50,
            "Free delivery in 7 days", "King size beds", 4.6f)
        insertSubcategory(categoryId, "Dining Tables", R.drawable.img_25, 9999.0, 19999.0, 50,
            "Free delivery in 4 days", "6-seater dining tables", 4.5f)
        insertSubcategory(categoryId, "Wardrobes", R.drawable.img_31, 14999.0, 29999.0, 50,
            "Free delivery in 5 days", "Sliding door wardrobes", 4.4f)
        insertSubcategory(categoryId, "Lighting", R.drawable.img_33, 2999.0, 5999.0, 50,
            "Free delivery in 2 days", "Modern LED lighting", 4.3f)
    }

    private fun insertBeautySubcategories(categoryId: Long) {
        insertSubcategory(categoryId, "Shampoos", R.drawable.img_38, 399.0, 799.0, 50,
            "Free delivery in 2 days", "Hair care shampoos", 4.5f)
        insertSubcategory(categoryId, "Perfumes", R.drawable.img_40, 999.0, 1999.0, 50,
            "Free delivery in 3 days", "Premium fragrances", 4.7f)
        insertSubcategory(categoryId, "Makeup", R.drawable.img_42, 999.0, 1999.0, 50,
            "Free delivery in 2 days", "Cosmetic makeup kits", 4.6f)
        insertSubcategory(categoryId, "Skincare", R.drawable.img_41, 499.0, 999.0, 50,
            "Free delivery in 2 days", "Face creams and lotions", 4.4f)
        insertSubcategory(categoryId, "Hair Care", R.drawable.img_35, 399.0, 799.0, 50,
            "Free delivery in 3 days", "Hair oils and serums", 4.5f)
    }

    private fun insertToySubcategories(categoryId: Long) {
        insertSubcategory(categoryId, "Action Figures", R.drawable.img_45, 499.0, 999.0, 50,
            "Free delivery in 2 days", "Superhero action figures", 4.5f)
        insertSubcategory(categoryId, "Board Games", R.drawable.img_50, 799.0, 1499.0, 48,
            "Free delivery in 3 days", "Family board games", 4.7f)
        insertSubcategory(categoryId, "LEGO Sets", R.drawable.img_47, 1499.0, 2999.0, 50,
            "Free delivery in 4 days", "Building block sets", 4.8f)
        insertSubcategory(categoryId, "Dolls", R.drawable.img_46, 799.0, 1499.0, 48,
            "Free delivery in 3 days", "Fashion dolls", 4.6f)
        insertSubcategory(categoryId, "Remote Cars", R.drawable.img_49, 999.0, 1999.0, 50,
            "Free delivery in 2 days", "RC toy cars", 4.5f)
    }

    private fun insertSportsSubcategories(categoryId: Long) {
        insertSubcategory(categoryId, "Cricket", R.drawable.img_51, 1499.0, 2999.0, 50,
            "Free delivery in 3 days", "Cricket bats and balls", 4.7f)
        insertSubcategory(categoryId, "Football", R.drawable.img_52, 999.0, 1999.0, 50,
            "Free delivery in 2 days", "Football shoes and balls", 4.6f)
        insertSubcategory(categoryId, "Badminton", R.drawable.img_59, 999.0, 1999.0, 50,
            "Free delivery in 2 days", "Rackets and shuttlecocks", 4.5f)
        insertSubcategory(categoryId, "Tennis", R.drawable.img_53, 1999.0, 3999.0, 50,
            "Free delivery in 3 days", "Tennis rackets and balls", 4.7f)
        insertSubcategory(categoryId, "Gym Equipment", R.drawable.img_57, 4999.0, 9999.0, 50,
            "Free delivery in 5 days", "Home gym equipment", 4.8f)
    }
    // End Region

    private fun navigateToSubcategory(category: Category) {
        Intent(this, SubcategoryProductsActivity::class.java).apply {
            putExtra("CATEGORY_ID", category.id)
            putExtra("CATEGORY_NAME", category.name)
            putExtra("USER_ID", userId)
            startActivity(this)
        }
    }

    private fun setupBottomNavigation() {
        findViewById<BottomNavigationView>(R.id.bottomNavigation).apply {
            selectedItemId = R.id.bottom_category
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.bottom_home -> {
                        navigateTo(home::class.java)
                        true
                    }
                    R.id.bottom_profile -> {
                        navigateTo(profile::class.java)
                        true
                    }
                    R.id.bottom_cart -> {
                        navigateTo(cart::class.java)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun navigateTo(activity: Class<*>) {
        startActivity(Intent(this, activity).apply {
            putExtra("USER_ID", userId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        })
        finish()
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}