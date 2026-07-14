package com.example.shopnest

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.shopnest.databinding.ActivitySignupBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class signup : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)

        // Initialize DatabaseHelper
        databaseHelper = DatabaseHelper(this)

        // Apply animations
        applyAnimations()

        // Redirect to Sign-in page
        binding.sigintxt.setOnClickListener {
            startActivity(Intent(this, signin::class.java))
        }

        // Signup button click listener
        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                val name = etName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val password = etPassword.text.toString().trim()

                // Check if email or phone already exists
                if (databaseHelper.checkEmailExists(email)) {
                    etEmail.error = "Email already exists"
                    etEmail.requestFocus()
                    return@setOnClickListener
                }

                if (databaseHelper.checkPhoneExists(phone)) {
                    etPhone.error = "Phone number already exists"
                    etPhone.requestFocus()
                    return@setOnClickListener
                }

                // ⭐️ INSERT USER HERE
                val userId = databaseHelper.insertUser(name, email, password, phone)
                if (userId != -1L) {
                    Log.d("Signup", "New user ID: $userId")  // Log after signup
                    Log.d("IntentCheck", "Passing USER_ID: $userId")

                    val intent = Intent(this, signin::class.java)
                    intent.putExtra("USER_ID", userId)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("Signup", "User signup failed")
                }

                val intent = Intent(this, otp::class.java)
                intent.putExtra("name", name)
                intent.putExtra("email", email)
                intent.putExtra("phone", phone)
                intent.putExtra("password", password)
                startActivity(intent)

            }
        }

    }

    private fun applyAnimations() {
        val fadeIn = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val bottomDown = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.bottom_down)

        binding.topLinearLayout.animation = bottomDown
        Handler().postDelayed({
            binding.cardView.animation = fadeIn
            binding.cardView1.animation = fadeIn
            binding.compwdInputLayout.animation = fadeIn
            binding.emailInputLayout.animation = fadeIn
            binding.phoneInputLayout.animation = fadeIn
            binding.etPassword.animation = fadeIn
            binding.etConfirmPassword.animation = fadeIn
        }, 1000)
    }

    private fun validateInputs(): Boolean {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        if (name.isEmpty()) {
            etName.error = "Name is required"
            etName.requestFocus()
            return false
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email"
            etEmail.requestFocus()
            return false
        }

        if (phone.isEmpty() || phone.length != 10 || !phone.all { it.isDigit() }) {
            etPhone.error = "Enter a valid 10-digit phone number"
            etPhone.requestFocus()
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return false
        }

        if (confirmPassword.isEmpty() || confirmPassword != password) {
            etConfirmPassword.error = "Passwords do not match"
            etConfirmPassword.requestFocus()
            return false
        }

        return true
    }
}