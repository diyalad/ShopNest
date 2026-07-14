package com.example.shopnest

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.shopnest.databinding.ActivityBuyNowBinding
import com.google.android.material.textview.MaterialTextView
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import android.widget.Toast
import com.example.shopnest.Model.Address
import com.example.shopnest.Model.Subcategory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import com.bumptech.glide.Glide
import java.util.Locale

class BuyNowActivity : AppCompatActivity(), PaymentResultListener {

    companion object {
        private const val REQUEST_SELECT_ADDRESS = 1001
    }

    private lateinit var binding: ActivityBuyNowBinding
    private lateinit var step1: MaterialTextView
    private lateinit var step2: MaterialTextView
    private lateinit var step3: MaterialTextView
    private lateinit var line1: View
    private lateinit var line2: View
    private var quantity: Int = 1
    private var productId: Int = -1
    private var productPrice: Double = 0.0
    private var userId: Long = -1
    private var rating: Float = 0.0f
    private lateinit var dbHelper: DatabaseHelper
    private var subcategory: Subcategory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuyNowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Checkout.preload(applicationContext)
        initializeViews()
        handleIntentData()
        setupClickListeners()
        loadInitialData()
    }

    private fun initializeViews() {
        step1 = findViewById(R.id.step1)
        step2 = findViewById(R.id.step2)
        step3 = findViewById(R.id.step3)
        line1 = findViewById(R.id.line1)
        line2 = findViewById(R.id.line2)
        setStepActive(step1, line1, step2)
    }

    private fun handleIntentData() {
        userId = intent.getLongExtra("USER_ID", -1).also {
            if (it == -1L) {
                showLoginError()
                return
            }
        }

        productId = intent.getIntExtra("PRODUCT_ID", -1)
        dbHelper = DatabaseHelper.getInstance(this)

        // Fetch product details from the database
        subcategory = fetchSubcategoryFromDatabase(productId)
        if (subcategory == null) {
            Log.e("BuyNowActivity", "Failed to fetch product details from database")
            Toast.makeText(this, "Failed to load product details", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        productPrice = subcategory?.discountedPrice ?: 0.0
        rating = subcategory?.rating ?: 0.0f

        binding.productName.text = subcategory?.name

        // Use Glide to load the image
        subcategory?.imagePath?.let { imagePath ->
            Glide.with(this)
                .load(File(imagePath))
                .placeholder(R.drawable.toy1) // fallback image
                .error(R.drawable.toy1) // error image
                .into(binding.productImage)
        } ?: run {
            binding.productImage.setImageResource(R.drawable.toy1)
        }

        binding.productPrice.text = "₹${productPrice.toInt()}"
        binding.productSize.text = "Selected Size: ${intent.getStringExtra("SELECTED_SIZE")}"
        binding.deliveryInfo.text = subcategory?.deliveryInfo
        binding.productRating.text = "${rating} ★ (${getRandomReviewCount()})"

        val originalPrice = subcategory?.originalPrice ?: 0.0
        val savings = originalPrice - productPrice
        binding.savingsText.text = "You'll save ₹${savings.toInt()}"
        binding.priceDetails.text = "₹${productPrice.toInt()}"
        binding.discountDetails.text = "-₹${savings.toInt()}"
        binding.totalAmount.text = "₹${productPrice.toInt()}"
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

    private fun showLoginError() {
        Log.e("BuyNowActivity", "User ID not found")
        Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, signin::class.java))
        finish()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener { onBackPressed() }

        binding.changeAddressButton.setOnClickListener {
            Intent(this, SelectAddressActivity::class.java).apply {
                putExtra("USER_ID", userId)
                startActivityForResult(this, REQUEST_SELECT_ADDRESS)
            }
        }

        binding.proceedToPaymentButton.setOnClickListener {
            if (binding.deliveryAddress.text.contains("No address found")) {
                Toast.makeText(this, "Please add a delivery address", Toast.LENGTH_SHORT).show()
            } else {
                startPayment(productPrice * quantity)
            }
        }

        binding.minusButton.setOnClickListener {
            if (quantity > 1) updateQuantity(--quantity)
        }

        binding.plusButton.setOnClickListener {
            updateQuantity(++quantity)
        }
    }

    private fun getRandomReviewCount(): String {
        return "${(1000..50000).random()}"
    }

    private fun loadInitialData() {
        val addresses = dbHelper.getAddressesByUserId(userId)

        if (addresses.isNotEmpty()) {
            binding.deliveryAddress.text = formatAddress(addresses[0])
        } else {
            binding.deliveryAddress.text = "No address found. Please add an address."
        }
    }

    private fun formatAddress(address: Address): String {
        return """
            ${address.fullName}
            ${address.houseNo}, ${address.roadName}
            ${address.city}, ${address.state} - ${address.pincode}
            Phone: ${address.phone}
            ${address.alternatePhone?.takeIf { it.isNotBlank() }?.let { "Alternate: $it" } ?: ""}
        """.trimIndent()
    }

    private fun updateQuantity(newQuantity: Int) {
        quantity = newQuantity
        binding.quantityTextView.text = quantity.toString()
        binding.totalAmount.text = "₹${(productPrice * quantity).toInt()}"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_ADDRESS && resultCode == RESULT_OK) {
            data?.getStringExtra("SELECTED_ADDRESS")?.let {
                binding.deliveryAddress.text = it
            }
        }
    }

    private fun startPayment(amount: Double) {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_Y7YnikR706OvX9")

        try {
            JSONObject().apply {
                put("name", "ShopNest")
                put("description", "Payment for your order")
                put("currency", "INR")
                put("amount", (amount * 100).toInt())
                put("prefill.email", "user@example.com")
                put("prefill.contact", "9876543210")
                checkout.open(this@BuyNowActivity, this)
            }
        } catch (e: Exception) {
            Log.e("BuyNowActivity", "Payment error", e)
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        dbHelper.storeOrderHistory(
            userId,
            productId,
            quantity,
            productPrice * quantity,
            razorpayPaymentId ?: "",
            "Success",
            formatter.format(calendar.apply { add(Calendar.DAY_OF_YEAR, 1) }.time),
            formatter.format(calendar.apply { add(Calendar.DAY_OF_YEAR, 2) }.time),
            intent.getStringExtra("SELECTED_SIZE") ?: "",
            dbHelper.getAddressesByUserId(userId)[0].addressId
        )

        startActivity(Intent(this, OrderHistoryActivity::class.java))
    }

    override fun onPaymentError(errorCode: Int, errorMessage: String?) {
        Toast.makeText(this, "Payment failed: $errorMessage", Toast.LENGTH_SHORT).show()
    }

    private fun setStepActive(activeStep: MaterialTextView, activeLine: View, nextStep: MaterialTextView) {
        activeStep.setBackgroundResource(R.drawable.circle_background)
        activeStep.setTextColor(Color.WHITE)
        activeLine.setBackgroundColor(resources.getColor(R.color.deep_blue))
        nextStep.setBackgroundResource(R.drawable.circle_background_gray)
        nextStep.setTextColor(Color.BLACK)
    }
}
