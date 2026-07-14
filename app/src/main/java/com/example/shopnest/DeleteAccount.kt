package com.example.shopnest

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog

import com.example.shopnest.signin

class DeleteAccount : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_account)

        // Back button functionality
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Closes the current activity and goes back
        }

        // Cancel button functionality
        val btnCancel: Button = findViewById(R.id.btnCancel)
        btnCancel.setOnClickListener {
            finish() // Simply closes the activity
        }

        // Continue button functionality
        val btnContinue: Button = findViewById(R.id.btnContinue)
        btnContinue.setOnClickListener {
            showConfirmationDialog()
        }
    }

    // Show confirmation dialog before deleting account
    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Deletion")
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.")

        builder.setPositiveButton("Yes, Delete") { _: DialogInterface, _: Int ->
            deleteAccount()
        }

        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    // Account deletion logic (Placeholder for actual implementation)
    private fun deleteAccount() {
        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_LONG).show()

        // Redirect to login or welcome screen after deletion
//        val intent = Intent(this, signin::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(intent)
    }
}
