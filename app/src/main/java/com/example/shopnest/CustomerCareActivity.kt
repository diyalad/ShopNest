package com.example.shopnest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.shopnest.databinding.ActivityCustomerCareBinding
import com.google.android.material.card.MaterialCardView

class CustomerCareActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerCareBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerCareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Customer Care"

        // Get current user details
        val userId = intent.getLongExtra("USER_ID", -1)
        val user = dbHelper.getUserById(userId)
        val username = user?.name ?: "ShopNest User"

        // Set up contact cards
        setupContactCard(
            binding.maleContactCard,
            binding.maleContactImage,
            binding.maleContactName,
            binding.maleContactNumber,
            "John Doe",
            "+911234567890",
            username,
            R.id.callIcon1,
            R.id.messageIcon1,
            R.id.whatsappIcon1
        )

        setupContactCard(
            binding.femaleContactCard,
            binding.femaleContactImage,
            binding.femaleContactName,
            binding.femaleContactNumber,
            "Jane Smith",
            "+913214567890",
            username,
            R.id.callIcon2,
            R.id.messageIcon2,
            R.id.whatsappIcon2
        )
    }

    private fun setupContactCard(
        card: MaterialCardView,
        imageView: ImageView,
        nameView: TextView,
        numberView: TextView,
        name: String,
        number: String,
        username: String,
        callIconId: Int,
        messageIconId: Int,
        whatsappIconId: Int
    ) {
        // Set contact details
        nameView.text = name
        numberView.text = number

        // Set up click listeners for action buttons
        card.findViewById<ImageView>(callIconId).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$number")
            startActivity(intent)
        }

        card.findViewById<ImageView>(messageIconId).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("smsto:$number")
            intent.putExtra("sms_body", "Hello from ShopNest user: $username")
            startActivity(intent)
        }

        card.findViewById<ImageView>(whatsappIconId).setOnClickListener {
            try {
                val url = "https://api.whatsapp.com/send?phone=$number&text=Hello from ShopNest user $username"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            } catch (e: Exception) {
                // WhatsApp not installed
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp")
                startActivity(intent)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}