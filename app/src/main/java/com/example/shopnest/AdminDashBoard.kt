package com.example.shopnest

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class AdminDashBoard : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dash_board)

        // Find all cards
        val categCard = findViewById<CardView>(R.id.categorycard)
        val productCard = findViewById<CardView>(R.id.productcard)
        val favCard = findViewById<CardView>(R.id.favoitescard)
        val orderCard = findViewById<CardView>(R.id.ordercard)
        val cartCard = findViewById<CardView>(R.id.cartcard)
        val reportsCard = findViewById<CardView>(R.id.reportscard)
        val logoutCard = findViewById<CardView>(R.id.logoutcard)

        dbHelper = DatabaseHelper(this)

        // Set click listeners
        categCard.setOnClickListener {
             val i = Intent(this, AdminCategoryActivity::class.java)
             startActivity(i)
        }

        productCard.setOnClickListener {
            // val i = Intent(this, AdminBookListActivity::class.java)
            // startActivity(i)
        }

        favCard.setOnClickListener {
            val i = Intent(this, AdminFavoritesActivity::class.java)
            startActivity(i)
        }

        orderCard.setOnClickListener {
            val i = Intent(this, AdminOrderHistoryActivity::class.java)
            startActivity(i)
        }

        cartCard.setOnClickListener {
            val i = Intent(this, AdminCartActivity::class.java)
            startActivity(i)
        }

        logoutCard.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        reportsCard.setOnClickListener {
            val i = Intent(this, ReportsActivity::class.java)
            startActivity(i)
        }
    }

    private fun showLogoutConfirmationDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.logout_dialog, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val btnNo = dialogView.findViewById<Button>(R.id.btnNo)
        val btnYes = dialogView.findViewById<Button>(R.id.btnYes)

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        btnYes.setOnClickListener {
            // Clear any admin session data if needed
            // dbHelper.clearAdminSession() - implement this in DatabaseHelper if needed

            // Redirect to login/signin activity
            val intent = Intent(this, signin::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }
}