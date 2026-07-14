package com.example.shopnest

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopnest.Model.AdminCartItem

class AdminCartActivity : AppCompatActivity() {

    private lateinit var rvCartItems: RecyclerView
    private lateinit var tvEmptyCart: TextView
    private lateinit var tvSummary: TextView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_cart)

        // Initialize views
        rvCartItems = findViewById(R.id.rvCartItems)
        tvEmptyCart = findViewById(R.id.tvEmptyCart)
        tvSummary = findViewById(R.id.tvSummary)
        dbHelper = DatabaseHelper(this)

        // Setup RecyclerView
        setupRecyclerView()

        // Load cart items
        loadCartItems()

        // Set click listener for summary button
        tvSummary.setOnClickListener {
            showSummaryPopup()
        }
    }

    private fun setupRecyclerView() {
        rvCartItems.layoutManager = LinearLayoutManager(this)
        rvCartItems.setHasFixedSize(true)
    }

    private fun loadCartItems() {
        val cartItems = dbHelper.getAllAdminCartItems()

        if (cartItems.isEmpty()) {
            showEmptyState()
        } else {
            showCartItems(cartItems)
        }
    }

    private fun showEmptyState() {
        rvCartItems.visibility = View.GONE
        tvEmptyCart.visibility = View.VISIBLE
    }

    private fun showCartItems(cartItems: List<AdminCartItem>) {
        rvCartItems.visibility = View.VISIBLE
        tvEmptyCart.visibility = View.GONE
        rvCartItems.adapter = CartItemsAdapter(cartItems)
    }

    private fun showSummaryPopup() {
        val cartItems = dbHelper.getAllAdminCartItems()
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_admin_cart_summary, null)

        // Calculate summary metrics
        val totalProducts = cartItems.size
        val totalQuantity = cartItems.sumOf { it.quantity }
        val totalRevenue = cartItems.sumOf { it.productPrice * it.quantity }
        val totalDiscount = cartItems.sumOf { (it.productPrice * it.discount / 100) * it.quantity }
        val netRevenue = totalRevenue - totalDiscount

        // Set values to popup views
        popupView.findViewById<TextView>(R.id.tvTotalProducts).text = "🛍️ Products in cart: $totalProducts"
        popupView.findViewById<TextView>(R.id.tvTotalQuantity).text = "🧮 Total quantity: $totalQuantity"
        popupView.findViewById<TextView>(R.id.tvTotalRevenue).text = "💰 Gross revenue: ₹${"%.2f".format(totalRevenue)}"
        popupView.findViewById<TextView>(R.id.tvTotalDiscount).text = "🎁 Total discount: ₹${"%.2f".format(totalDiscount)}"
        popupView.findViewById<TextView>(R.id.tvNetRevenue).text = "💵 Net revenue: ₹${"%.2f".format(netRevenue)}"

        // Create and show popup
        val popup = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(ContextCompat.getDrawable(this@AdminCartActivity, android.R.color.transparent))
            isOutsideTouchable = true
            elevation = 10f
            showAsDropDown(tvSummary, (-tvSummary.width * 1.5).toInt(), 0, Gravity.END)
        }
    }

    inner class CartItemsAdapter(private val cartItems: List<AdminCartItem>) :
        RecyclerView.Adapter<CartItemsAdapter.CartItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cart_admin, parent, false)
            return CartItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
            holder.bind(cartItems[position])
        }

        override fun getItemCount(): Int = cartItems.size

        inner class CartItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
            private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
            private val tvUserInfo: TextView = itemView.findViewById(R.id.tvUserInfo)
            private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
            private val tvDiscount: TextView = itemView.findViewById(R.id.tvDiscount)
            private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
            private val tvTotalPrice: TextView = itemView.findViewById(R.id.tvTotalPrice)

            fun bind(cartItem: AdminCartItem) {
                ivProductImage.setImageResource(cartItem.productImageResId)
                tvProductName.text = cartItem.productName
                tvUserInfo.text = "👤 User: ${cartItem.userName} (${cartItem.userEmail})"
                tvPrice.text = "Price: ₹${"%.2f".format(cartItem.productPrice)}"

                if (cartItem.discount > 0) {
                    tvDiscount.visibility = View.VISIBLE
                    tvDiscount.text = "🎁 Discount: ${cartItem.formattedDiscount()}"
                } else {
                    tvDiscount.visibility = View.GONE
                }

                tvQuantity.text = "🔢 Qty: ${cartItem.quantity}"
                tvTotalPrice.text = "💰 Total: ${cartItem.formattedTotalPrice()}"
            }
        }
    }
}