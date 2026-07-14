package com.example.shopnest

import TranslationUtil
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopnest.Model.Category
import com.example.shopnest.Model.Subcategory
import com.example.shopnest.adapters.HomeCategoryAdapter
import com.example.shopnest.adapters.HomeSubcategoryAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class home : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var subcategoryRecyclerView: RecyclerView
    private lateinit var homeCategoryAdapter: HomeCategoryAdapter
    private lateinit var homeSubcategoryAdapter: HomeSubcategoryAdapter
    private lateinit var categoryList: List<Category>
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Long = -1
    private var subcategoryList: ArrayList<Subcategory> = ArrayList()
    private var customerCareFab: FloatingActionButton? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize views
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        toolbar = findViewById(R.id.toolbar)
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView)
        subcategoryRecyclerView = findViewById(R.id.subcategoryRecyclerView)
        customerCareFab = findViewById(R.id.customerCareFab)

        // Initialize DatabaseHelper
        dbHelper = DatabaseHelper(this)

        // Retrieve userId from SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        userId = sharedPref.getLong("userId", -1L)

        if (userId == -1L) {
            Log.e("HomeActivity", "User ID not found in SharedPreferences")
        } else {
            Log.d("HomeActivity", "User ID retrieved: $userId")
            fetchUserDetails(userId)
        }

        setupCategoryRecyclerView()
        setupSubcategoryRecyclerView()

        setSupportActionBar(toolbar)
        setupNavigationDrawer()

        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.nav_open,
            R.string.nav_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()



        // Bottom Navigation item selection
        bottomNavigationView.selectedItemId = R.id.bottom_home
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> true
                R.id.bottom_category -> {
                    startActivity(Intent(applicationContext, CategoryActivity::class.java).apply {
                        putExtra("USER_ID", userId)
                    })
                    true
                }
                R.id.bottom_cart -> {
                    startActivity(Intent(applicationContext, cart::class.java).apply {
                        putExtra("USER_ID", userId)
                    })
                    true
                }
                R.id.bottom_profile -> {
                    startActivity(Intent(applicationContext, profile::class.java))
                    true
                }
                else -> false
            }
        }

        applyTranslations()
        customerCareFab?.setOnClickListener {
            val intent = Intent(this, CustomerCareActivity::class.java).apply {
                putExtra("USER_ID", userId)
            }
            startActivity(intent)
        }


    }

    private fun setupCategoryRecyclerView() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoryRecyclerView.layoutManager = layoutManager

        // Load categories
        categoryList = dbHelper.getAllCategories()

        homeCategoryAdapter = HomeCategoryAdapter(categoryList) { category ->
            startActivity(Intent(this, CategoryActivity::class.java).apply {
                putExtra("CATEGORY_ID", category.id)
                putExtra("USER_ID", userId)
                putExtra("SELECTED_CATEGORY", category.name)
            })
        }
        categoryRecyclerView.adapter = homeCategoryAdapter
    }

    private fun setupSubcategoryRecyclerView() {
        // Initialize layout manager and decoration
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        subcategoryRecyclerView.layoutManager = layoutManager
        subcategoryRecyclerView.addItemDecoration(GridSpacingItemDecoration(1, dpToPx(8), true))

        // Initialize with empty list first
        subcategoryList = ArrayList()

        // Create adapter with click listeners
        homeSubcategoryAdapter = HomeSubcategoryAdapter(
            subcategoryList,
            onSubcategoryClicked = { subcategory ->
                startActivity(Intent(this, CategoryActivity::class.java).apply {
                    putExtra("SUBCATEGORY_ID", subcategory.id)
                    putExtra("USER_ID", userId)
                    putExtra("SUBCATEGORY_NAME", subcategory.name)
                })
            },
            onAddToCartClicked = { subcategory ->
                // Add to cart logic
                Toast.makeText(this, "${subcategory.name} added to cart", Toast.LENGTH_SHORT).show()
            }
        )


        // Set adapter to RecyclerView
        subcategoryRecyclerView.adapter = homeSubcategoryAdapter

        // Load data asynchronously
        loadSubcategories()

        // Center item detection for horizontal scrolling
        subcategoryRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val centerX = (recyclerView.left + recyclerView.right) / 2
                var closestItem = 0
                var closestDistance = Int.MAX_VALUE

                for (i in 0 until recyclerView.childCount) {
                    val child = recyclerView.getChildAt(i)
                    val childCenter = (child.left + child.right) / 2
                    val distance = abs(centerX - childCenter)

                    if (distance < closestDistance) {
                        closestDistance = distance
                        closestItem = recyclerView.getChildAdapterPosition(child)
                    }
                }

                // Post the update to run after the current layout pass
                recyclerView.post {
                    homeSubcategoryAdapter.setCenteredItemPosition(closestItem)
                }
            }
        })
    }

    private fun loadSubcategories() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val subcategories = dbHelper.getAllSubcategories()

                withContext(Dispatchers.Main) {
                    subcategoryList.clear()
                    subcategoryList.addAll(subcategories)
                    homeSubcategoryAdapter.notifyDataSetChanged()

                    // Set initial centered position
                    if (subcategoryList.isNotEmpty()) {
                        homeSubcategoryAdapter.setCenteredItemPosition(0)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("HomeActivity", "Error loading subcategories", e)
                    Toast.makeText(this@home, "Error loading products", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    inner class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount
                if (position < spanCount) outRect.top = spacing
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) outRect.top = spacing
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    private fun setupNavigationDrawer() {
        val headerView: View? = navigationView.getHeaderView(0)
        headerView?.let {
            val profileImage: ImageView? = it.findViewById(R.id.profileImage)
            profileImage?.setOnClickListener {
                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
                val userId = sharedPref.getLong("userId", -1L)

                if (isLoggedIn && userId != -1L) {
                    startActivity(Intent(this, EditProfile::class.java).apply {
                        putExtra("USER_ID", userId)
                    })
                } else {
                    startActivity(Intent(this, signin::class.java))
                    finish()
                }
            }
        }

        drawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bottom_home -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_category -> {
                    startActivity(Intent(this, CategoryActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_my_profile -> {
                    startActivity(Intent(this, profile::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_fav -> {
                    startActivity(Intent(this, FavouritesActivitly::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_reviews -> {
                    startActivity(Intent(this, RatingActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_order_list -> {
                    startActivity(Intent(this, OrderHistoryActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_share -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Check out this awesome app!")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "I found this great app! You should try it out: https://play.google.com/store/apps/details?id=your.package.name"
                        )
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share via"))
                    true
                }
                R.id.nav_logout -> {
                    showLogoutConfirmationDialog()
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.favorite -> {
                startActivity(Intent(this, FavouritesActivitly::class.java))
                return true
            }
            R.id.cart -> {
                startActivity(Intent(this, cart::class.java).apply {
                    putExtra("USER_ID", userId)
                })
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                logoutUser()
            }
            .setNegativeButton("No") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .show()
    }

    private fun logoutUser() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, signin::class.java))
        finish()
    }

    private fun fetchUserDetails(userId: Long) {
        val user = dbHelper.getUserById(userId)
        if (user != null) {
            Log.d("HomeActivity", "User details: ${user.name}, ${user.email}, ${user.phone}")
        } else {
            Log.e("HomeActivity", "User not found in the database")
        }
    }

    private fun refreshSubcategories() {
        val subcategoryListFromDB = dbHelper.getAllSubcategories()
        subcategoryList.clear()
        subcategoryList.addAll(subcategoryListFromDB)
        homeSubcategoryAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        refreshSubcategories()
    }

    override fun translateActivityText() {
        val savedLanguageCode = TranslationUtil.getSavedLanguage(this)
        if (savedLanguageCode == TranslateLanguage.ENGLISH) return

        val textViewsToTranslate = listOf(
            findViewById<TextView>(R.id.productTitleTV),
            findViewById<TextView>(R.id.categoryTitleTV)
        )

        textViewsToTranslate.forEach { textView ->
            val textToTranslate = textView?.text.toString()
            TranslationUtil.translateText(
                textToTranslate,
                targetLanguage = savedLanguageCode,
                onSuccess = { translatedText ->
                    runOnUiThread {
                        textView?.text = translatedText
                    }
                },
                onFailure = { exception ->
                    println("Translation failed: ${exception.message}")
                }
            )
        }
    }

    private fun applyTranslations() {
        val savedLanguageCode = TranslationUtil.getSavedLanguage(this)
        println("Applying translations for language: $savedLanguageCode")
        if (savedLanguageCode != TranslateLanguage.ENGLISH) {
            translateActivityText()
        }
    }
}