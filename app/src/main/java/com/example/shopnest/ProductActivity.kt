package com.example.shopnest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopnest.Model.Product
import com.example.shopnest.Model.Subcategory
import com.example.shopnest.adapters.ProductAdapter


class ProductActivity : AppCompatActivity() {

    private lateinit var productRecyclerView: RecyclerView
    private lateinit var dbHelper: DatabaseHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        // Initialize DatabaseHelper
        dbHelper = DatabaseHelper(this)
        dbHelper.writableDatabase

        // Insert sample data (only once)
        // insertSampleData()

        // Get subcategory name from intent
        val subcategoryName = intent.getStringExtra("SUBCATEGORY_NAME")

        // Set up RecyclerView
        productRecyclerView = findViewById(R.id.productRecyclerView)
        productRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load products dynamically based on subcategory
        // val productList = loadProducts(subcategoryName)

        // Set up adapter
//        val adapter = ProductAdapter(
//            //productList = productList,
//            onProductClicked = { product ->
//                // Handle product click
//            },
//            onAddToCartClicked = { product ->
//                // Handle add to cart click
//            }
//        )
//
//        productRecyclerView.adapter = adapter
//    }

//    private fun insertSampleData() {
//        // Check if products are already inserted (optional to avoid duplicates)
//        //val products = dbHelper.getAllProducts()
//        if (products.isNotEmpty()) {
//            return // Skip inserting again
//        }
//
//        // Get all subcategories
//        val subcategories: List<Subcategory> = dbHelper.getAllSubcategories()
//
//        // Find the desired subcategory IDs
//        val casualShirtsSubcategory = subcategories.find { it.name == "CasualShirts" }
//        val tshirtPolosSubcategory = subcategories.find { it.name == "T-Shirt&Polos" }
//
//        // Insert products for Casual Shirts subcategory
//        casualShirtsSubcategory?.let { subcat ->
//            dbHelper.insertProduct(
//                name = "Men's Shirt",
//                discountedPrice = 1799.0,
//                originalPrice = 4999.0,
//                discountPercentage = 64,
//                imageResId = R.drawable.shirt,
//                deliveryInfo = "FREE delivery Fri, 21 Mar",
//                rating = 4.5, // Sample value
//                reviews = 250, // Sample value
//                boughtCount = 150, // Sample value
//                subcategoryId = subcat.id
//            )
//
//            dbHelper.insertProduct(
//                name = "Casual Shirt",
//                discountedPrice = 1479.0,
//                originalPrice = 3999.0,
//                discountPercentage = 63,
//                imageResId = R.drawable.shirt,
//                deliveryInfo = "FREE delivery Thu, 20 Mar",
//                rating = 4.2, // Sample value
//                reviews = 250, // Sample value
//                boughtCount = 150, // Sample value
//                subcategoryId = subcat.id
//            )
//        }
//
//        // Insert products for T-Shirts & Polos subcategory (Optional)
//        tshirtPolosSubcategory?.let { subcat ->
//            dbHelper.insertProduct(
//                name = "Basic T-Shirt",
//                discountedPrice = 799.0,
//                originalPrice = 1499.0,
//                discountPercentage = 46,
//                imageResId = R.drawable.tshirtandpolos,
//                deliveryInfo = "FREE delivery Wed, 19 Mar",
//                rating = 4.3, // Sample value
//                reviews = 180, // Sample value
//                boughtCount = 100, // Sample value
//                subcategoryId = subcat.id
//            )
//        }
//    }


//    private fun loadProducts(subcategoryName: String?): List<Product> {
//        val subcategories: List<Subcategory> = dbHelper.getAllSubcategories()
//        val subcategory = subcategories.find { it.name == subcategoryName }
//        return if (subcategory != null) {
//            dbHelper.getProductsBySubcategoryId(subcategory.id)
//        } else {
//            emptyList()
//        }
//    }
    }
}