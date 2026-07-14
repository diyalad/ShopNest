package com.example.shopnest

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shopnest.databinding.ActivityOtpBinding
import java.util.Random

class otp : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    private lateinit var phone: String // User's phone number
    private lateinit var verifybtn: Button
    private lateinit var resendTxt: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var countDownTimer: CountDownTimer
    private var isTimerRunning = false

    private var generatedOtp: String = "" // Store the generated OTP
    private val otpLength = 6 // Length of the OTP

    // User data from the signup activity
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var databaseHelper: DatabaseHelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize DatabaseHelper
        databaseHelper = DatabaseHelper(this)

        // Retrieve user data from the intent
        name = intent.getStringExtra("name") ?: ""
        email = intent.getStringExtra("email") ?: ""
        phone = intent.getStringExtra("phone") ?: ""
        password = intent.getStringExtra("password") ?: ""

        // Initialize views
        verifybtn = findViewById(R.id.btnVerifyOtp)
        resendTxt = findViewById(R.id.resend_txt)
        progressBar = findViewById(R.id.progressBar)

        // Display the phone number
        binding.phoneEd.text = phone

        // Generate and send OTP
        generateAndSendOtp()

        // Add TextWatchers to the OTP EditText fields
        setupOtpEditTexts()

        // Verify OTP button click listener
        verifybtn.setOnClickListener {
            val otp1 = binding.etOtp1.text.toString()
            val otp2 = binding.etOtp2.text.toString()
            val otp3 = binding.etOtp3.text.toString()
            val otp4 = binding.etOtp4.text.toString()
            val otp5 = binding.etOtp5.text.toString()
            val otp6 = binding.etOtp6.text.toString()

            val enteredOtp = otp1 + otp2 + otp3 + otp4 + otp5 + otp6

            if (enteredOtp.length == otpLength) {
                // Show the progress bar
                progressBar.visibility = ProgressBar.VISIBLE
                verifybtn.isEnabled = false

                // Simulate OTP verification with a delay
                verifyOtp(enteredOtp)
            } else {
                Toast.makeText(this, "Please enter a valid OTP", Toast.LENGTH_SHORT).show()
            }
        }

        // Resend OTP click listener
        resendTxt.setOnClickListener {
            if (!isTimerRunning) {
                generateAndSendOtp()
            } else {
                Toast.makeText(this, "Please wait before resending OTP", Toast.LENGTH_SHORT).show()
            }
        }

        // Apply animations
        applyAnimations()
    }

    private fun generateAndSendOtp() {
        // Generate a random OTP
        generatedOtp = generateRandomOtp(otpLength)

        // Simulate sending OTP to the user's phone (for demonstration, log it)
        Toast.makeText(this, "OTP sent: $generatedOtp", Toast.LENGTH_LONG).show()

        // Start the countdown timer for resend OTP
        startResendTimer()
    }

    private fun generateRandomOtp(length: Int): String {
        val random = Random()
        val sb = StringBuilder()
        for (i in 0 until length) {
            sb.append(random.nextInt(10)) // Append a random digit (0-9)
        }
        return sb.toString()
    }

    private fun verifyOtp(enteredOtp: String) {
        // Simulate OTP verification with a delay
        Handler().postDelayed({
            if (enteredOtp == generatedOtp) {
                // Hide the progress bar
                progressBar.visibility = ProgressBar.GONE

                // Save user data to the database
                val isInserted = databaseHelper.insertUser(name, email, phone, password)

                if (isInserted != -1L) {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    // Navigate to the home activity
                    startActivity(Intent(this, signin::class.java))
                    finish() // Close the current activity
                } else {
                    Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Hide the progress bar
                progressBar.visibility = ProgressBar.GONE
                verifybtn.isEnabled = true

                // Show error message
                Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }, 3000) // Simulate a 3-second delay for verification
    }

    private fun startResendTimer() {
        isTimerRunning = true
        resendTxt.isEnabled = false

        countDownTimer = object : CountDownTimer(20000, 1000) { // 20 seconds timer
            override fun onTick(millisUntilFinished: Long) {
                resendTxt.text = "Resend OTP in ${millisUntilFinished / 1000} seconds"
            }

            override fun onFinish() {
                isTimerRunning = false
                resendTxt.isEnabled = true
                resendTxt.text = "Resend OTP"
            }
        }.start()
    }

    private fun applyAnimations() {
        val fadeIn = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val bottomDown = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.bottom_down)
        binding.linearLayout.animation = bottomDown

        Handler().postDelayed({
            binding.cardView.animation = fadeIn
            binding.cardViewimage.animation = fadeIn
        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
    private fun setupOtpEditTexts() {
        val editTexts = listOf(
            binding.etOtp1,
            binding.etOtp2,
            binding.etOtp3,
            binding.etOtp4,
            binding.etOtp5,
            binding.etOtp6
        )

        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(OtpTextWatcher(editTexts, i))
        }
    }

    inner class OtpTextWatcher(
        private val editTexts: List<EditText>,
        private val currentIndex: Int
    ) : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s?.length == 1) {
                // Move focus to the next EditText
                if (currentIndex < editTexts.size - 1) {
                    editTexts[currentIndex + 1].requestFocus()
                }
            } else if (s.isNullOrEmpty() && currentIndex > 0) {
                // Move focus to the previous EditText if the current one is empty
                editTexts[currentIndex - 1].requestFocus()
            }
        }

        override fun afterTextChanged(s: Editable?) {}
    }
}