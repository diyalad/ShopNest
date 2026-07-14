package com.example.shopnest

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.shopnest.Model.User
import com.google.android.material.card.MaterialCardView

class RatingActivity : AppCompatActivity() {

    private lateinit var ratingBar: RatingBar
    private lateinit var feedbackEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var ratingStatusTextView: TextView
    private lateinit var completedRatingCardView: MaterialCardView
    private lateinit var completedTextView: TextView
    private lateinit var promptTextView: TextView

    private lateinit var dbHelper: DatabaseHelper
    private var user: User? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)

        ratingBar = findViewById(R.id.ratingBar)
        feedbackEditText = findViewById(R.id.feedbackEditText)
        submitButton = findViewById(R.id.submitButton)
        ratingStatusTextView = findViewById(R.id.ratingStatusTextView)
        completedRatingCardView = findViewById(R.id.completedRatingCardView)
        completedTextView = findViewById(R.id.completedTextView)
        promptTextView = findViewById(R.id.promptTextView)

        ratingBar.progressTintList = android.content.res.ColorStateList.valueOf(resources.getColor(android.R.color.holo_orange_light))
        ratingBar.secondaryProgressTintList = android.content.res.ColorStateList.valueOf(resources.getColor(android.R.color.holo_orange_light))

        dbHelper = DatabaseHelper(this)
        user = dbHelper.getUserById(1) // Replace 1 with the actual user ID you want to retrieve

        if (user == null) {
            Toast.makeText(this, "User not logged in ", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (dbHelper.hasUserRated(user!!.id)) {
            showCompletedRatingView(user!!.id)
        }

        submitButton.setOnClickListener {
            val stars = ratingBar.rating.toInt()
            val feedback = feedbackEditText.text.toString()

            if (stars == 0) {
                Toast.makeText(this, "Please select a rating ⭐", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = dbHelper.insertRating(user!!.id, stars, feedback)
            if (result) {
                showCompletedRatingView(user!!.id)
                Toast.makeText(this, "Rating submitted! 🎉", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to submit rating ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCompletedRatingView(userId: Long) {
        val rating = dbHelper.getUserRating(userId)
        if (rating != null) {
            promptTextView.visibility = View.GONE
            ratingBar.rating = rating.first.toFloat()
            ratingBar.isEnabled = false
            feedbackEditText.setText(rating.second)
            feedbackEditText.isEnabled = false
            submitButton.isEnabled = false
            ratingStatusTextView.text = "Your rating: ${rating.first} ⭐"
            completedRatingCardView.visibility = MaterialCardView.VISIBLE

            val emojiText = when {
                rating.first >= 4 -> "We are glad that you had a great experience in Learnify! 🎉🥳 Thank you for your feedback! 🙏"
                rating.first >= 3 -> "Thank you for your feedback! We'll keep improving! 👍"
                else -> "Thank you for your feedback. We'll work hard to improve your experience! 🔨"
            }
            completedTextView.text = emojiText
        }
    }
}