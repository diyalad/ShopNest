package com.example.shopnest

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopnest.adapters.CartAdapter
import com.example.shopnest.databinding.ActivityCartBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class cart : AppCompatActivity(), PaymentResultListener {

    private lateinit var binding: ActivityCartBinding
    private lateinit var adapter: CartAdapter
    private var userId: Long = -1
    private lateinit var dbHelper: DatabaseHelper
    private var totalAmount: Double = 0.0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Checkout.preload(applicationContext)
        dbHelper = DatabaseHelper.getInstance(this)
        userId = intent.getLongExtra("USER_ID", -1)
        if (userId == -1L) {
            finish()
            return
        }

        setupToolbar()
        setupAdapter()
        setupBottomNavigation()
        setupCheckoutButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupAdapter() {
        adapter = CartAdapter(mutableListOf()) { cartItem ->
            dbHelper.removeFromCart(cartItem.id)
            updateCartItems()
            updatePriceDetails()
        }
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.cartRecyclerView.adapter = adapter
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.bottom_cart
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> navigateTo(home::class.java)
                R.id.bottom_category -> navigateTo(CategoryActivity::class.java)
                R.id.bottom_profile -> navigateTo(profile::class.java)
                else -> false
            }
        }
    }

    private fun navigateTo(activity: Class<*>) : Boolean {
        startActivity(Intent(this, activity).apply {
            putExtra("USER_ID", userId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
            return true
    }

    private fun setupCheckoutButton() {
        binding.checkoutButton.setOnClickListener {
            if (adapter.itemCount > 0) {
                startPayment()
            } else {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateCartItems()
        updatePriceDetails()
    }

    private fun updateCartItems() {
        val cartItems = dbHelper.getCartItems(userId)
        adapter.updateItems(cartItems)

        binding.apply {
            cartRecyclerView.visibility = if (cartItems.isEmpty()) View.GONE else View.VISIBLE
            priceDetailsLayout.visibility = if (cartItems.isEmpty()) View.GONE else View.VISIBLE
            checkoutButton.visibility = if (cartItems.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun updatePriceDetails() {
        val cartItems = dbHelper.getCartItems(userId)
        val totalPrice = cartItems.sumOf { it.getDiscountedPrice() * it.quantity }
        val discount = calculateDiscount(totalPrice)
        val deliveryCharge = if (totalPrice > 500) 0.0 else 50.0
        totalAmount = totalPrice - discount + deliveryCharge

        binding.apply {
            totalItemsText.text = "Total Items: ${cartItems.size}"
            totalPriceText.text = "Total Price: ₹${"%.2f".format(totalPrice)}"
            discountText.text = "Discount: -₹${"%.2f".format(discount)}"
            deliveryChargesText.text = if (deliveryCharge > 0) {
                "Delivery Charges: ₹${"%.2f".format(deliveryCharge)}"
            } else {
                "Delivery Charges: FREE"
            }
            totalAmountText.text = "Total Amount: ₹${"%.2f".format(totalAmount)}"
        }
    }

    private fun calculateDiscount(totalPrice: Double): Double {
        return when {
            totalPrice > 1000 -> totalPrice * 0.10
            totalPrice > 500 -> totalPrice * 0.05
            else -> 0.0
        }
    }

    private fun startPayment() {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_Y7YnikR706OvX9")

        try {
            JSONObject().apply {
                put("name", "ShopNest")
                put("description", "Payment for your order")
                put("theme.color", "#FF5722")
                put("currency", "INR")
                put("amount", (totalAmount * 100).toInt())
                put("prefill.email", getEmailFromUser())
                put("prefill.contact", getPhoneFromUser())
            }.also { options ->
                checkout.open(this, options)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getEmailFromUser(): String {
        return dbHelper.getProfileByUserId(userId)?.email ?: "user@example.com"
    }

    private fun getPhoneFromUser(): String {
        return dbHelper.getProfileByUserId(userId)?.phone ?: "9876543210"
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        runOnUiThread {
            Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
            dbHelper.clearCart(userId)
            updateCartItems()
            updatePriceDetails()

            dbHelper.getCartItems(userId).forEach { item ->
                dbHelper.storeOrderHistory(
                    userId = userId,
                    subcategoryId = item.productId,
                    quantity = item.quantity,
                    totalPrice = item.getDiscountedPrice() * item.quantity,
                    paymentId = razorpayPaymentId ?: "",
                    paymentStatus = "Success",
                    deliveryStartDate = getDeliveryDate(1),
                    deliveryEndDate = getDeliveryDate(3),
                    size = "",
                    addressId = getDefaultAddressId()
                )
            }

            startActivity(Intent(this, OrderHistoryActivity::class.java).apply {
                putExtra("USER_ID", userId)
            })
        }
    }

    private fun getDeliveryDate(daysToAdd: Int): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, daysToAdd) }.time
        )
    }

    private fun getDefaultAddressId(): Long {
        return dbHelper.getAddressesByUserId(userId).firstOrNull()?.addressId ?: -1
    }

    override fun onPaymentError(code: Int, response: String?) {
        runOnUiThread {
            when (code) {
                Checkout.NETWORK_ERROR -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
                Checkout.INVALID_OPTIONS -> Toast.makeText(this, "Invalid options", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(this, "Payment failed: $response", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { onBackPressed(); true }
            R.id.favorite -> {
                startActivity(Intent(this, FavouritesActivitly::class.java).apply {
                    putExtra("USER_ID", userId)
                })
                true
            }
            R.id.cart -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}