package com.example.shopnest

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.shopnest.databinding.ActivitySigninBinding

class signin : AppCompatActivity() {
    private lateinit var binding: ActivitySigninBinding
    private lateinit var databaseHelper: DatabaseHelper

    // Admin credentials
    private companion object {
        const val ADMIN_USERNAME = "admin"
        const val ADMIN_PASSWORD = "admin123"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize View Binding
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize DatabaseHelper
        databaseHelper = DatabaseHelper(this)

        // Initialize animation
        val fadeIn = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val bottomDown = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.bottom_down)

        // Set bottom-down animation on top layout
        binding.linearLayout.animation = bottomDown

        // Handler for other layouts
        val handler = Handler()
        val runnable = Runnable {
            // Set fade-in animation on other layouts
            binding.cardView.animation = fadeIn
            binding.emailInputLayout.animation = fadeIn
            binding.pwdInputLayout.animation = fadeIn
            binding.fptext.animation = fadeIn
            binding.cardViewimage.animation = fadeIn
        }

        handler.postDelayed(runnable, 1000)

        // Sign-up text click event
        binding.siginuptxt.setOnClickListener {
            val intent = Intent(this, signup::class.java)
            startActivity(intent)
        }

        // Sign-in button click event
        binding.signinpbtn.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.pwd.text.toString().trim()

            if (validateInputs(email, password)) {
                // Check for admin login first
                if (email.equals(ADMIN_USERNAME, ignoreCase = true) && password == ADMIN_PASSWORD) {
                    // Admin login successful
                    Toast.makeText(this, "Admin login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, AdminDashBoard::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Check if the user exists in the database
                    if (databaseHelper.checkUserCredentials(email, password)) {
                        // Fetch user details from DB
                        val user = databaseHelper.getUser(email, password)

                        if (user != null) {
                            android.util.Log.d("Login", "User logged in: ${user.id}")
                            android.util.Log.d("IntentCheck", "Passing USER_ID: ${user.id}")

                            // Save userId and isLoggedIn to SharedPreferences
                            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                            val editor = sharedPref.edit()
                            editor.putBoolean("isLoggedIn", true)
                            editor.putLong("userId", user.id) // Save user ID
                            editor.apply()

                            Toast.makeText(this, "Sign-in successful!", Toast.LENGTH_SHORT).show()

                            // Go to Home Activity
                            val intent = Intent(this, home::class.java)
                            intent.putExtra("USER_ID", user.id) // Optional (already saved in prefs)
                            startActivity(intent)
                            finish() // Close sign-in activity
                        } else {
                            android.util.Log.e("Login", "User found in credentials check, but getUser() returned null!")
                            Toast.makeText(this, "Error fetching user details!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        android.util.Log.e("Login", "Invalid email or password")
                        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Forgot password text click event
        binding.fptext.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty() || (!Patterns.EMAIL_ADDRESS.matcher(email).matches() && email != ADMIN_USERNAME)) {
            binding.email.error = "Enter a valid email"
            binding.email.requestFocus()
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            binding.pwd.error = "Password must be at least 6 characters"
            binding.pwd.requestFocus()
            return false
        }

        return true
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        val emailOrPhoneInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.emailOrPhoneInput)
        val newPasswordInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.newPasswordInput)
        val resetButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.resetButton)

        resetButton.setOnClickListener {
            val emailOrPhone = emailOrPhoneInput.text.toString().trim()
            val newPassword = newPasswordInput.text.toString().trim()

            if (emailOrPhone.isEmpty()) {
                emailOrPhoneInput.error = "Enter your email or phone number"
                return@setOnClickListener
            }

            if (newPassword.isEmpty() || newPassword.length < 6) {
                newPasswordInput.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            // Check if the input is an email or phone number
            val isEmail = Patterns.EMAIL_ADDRESS.matcher(emailOrPhone).matches()
            val isPhone = Patterns.PHONE.matcher(emailOrPhone).matches()

            if (!isEmail && !isPhone) {
                emailOrPhoneInput.error = "Enter a valid email or phone number"
                return@setOnClickListener
            }

            // Check if the email or phone exists in the database
            val exists = if (isEmail) {
                databaseHelper.checkEmailExists(emailOrPhone)
            } else {
                databaseHelper.checkPhoneExists(emailOrPhone)
            }

            if (exists) {
                // Update the password
                if (databaseHelper.updatePassword(emailOrPhone, newPassword)) {
                    Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Failed to update password. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Email or phone number not found. Please check your input.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}