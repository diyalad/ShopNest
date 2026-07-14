package com.example.shopnest

import TranslationUtil
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.mlkit.nl.translate.TranslateLanguage

class profile : BaseActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var backButton: ImageView
    //    private lateinit var editProfileTV: TextView
    private lateinit var languageContainer: LinearLayout
    private var userId: Long = -1

    // Language map for mapping language names to language codes
    private val languageMap = mapOf(
        "English" to TranslateLanguage.ENGLISH,
        "हिंदी" to TranslateLanguage.HINDI,
        "தமிழ்" to TranslateLanguage.TAMIL,
        "తెలుగు" to TranslateLanguage.TELUGU,
        "ಕನ್ನಡ" to TranslateLanguage.KANNADA,
        "اردو" to TranslateLanguage.URDU
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize views
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        userId = intent.getLongExtra("USER_ID", -1)
        backButton = findViewById(R.id.backButton)
//        editProfileTV = findViewById(R.id.editProfileTV)
        languageContainer = findViewById(R.id.languageContainer)

        // Apply translations based on the saved language
        applyTranslations()

        // Handle bottom navigation
        bottomNavigationView.selectedItemId = R.id.bottom_profile
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    startActivity(Intent(this, home::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("USER_ID", userId)
                    })
                    finish()
                    true
                }
                R.id.bottom_category -> {
                    startActivity(Intent(this, CategoryActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("USER_ID", userId)
                    })
                    finish()
                    true
                }
                R.id.bottom_cart -> {
                    startActivity(Intent(this, cart::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("USER_ID", userId)
                    })
                    finish()
                    true
                }
                else -> false
            }
        }

        // Set click listeners for other views
//        editProfileTV.setOnClickListener {
//          val i = Intent(this , EditProfile::class.java)
//            startActivity(i)
//        }


        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            startActivity(Intent(this, home::class.java).apply {
                putExtra("USER_ID", userId)
            })
        }

        val logoutbtn = findViewById<MaterialButton>(R.id.Logout)
        logoutbtn.setOnClickListener{
            showLogoutConfirmationDialog()
        }

        val reviewTV = findViewById<TextView>(R.id.reviewTV)
        reviewTV.setOnClickListener{
            val i = Intent(this , RatingActivity::class.java)
            startActivity(i)
        }

//         val editProfileTV = findViewById<TextView>(R.id.editProfileTV)
//        editProfileTV.setOnClickListener{
//            val i = Intent(this, EditProfile::class.java)
//            startActivity(i)
//        }


        val saveAddressTV = findViewById<TextView>(R.id.saveAddressTV)
        saveAddressTV.setOnClickListener{
            val i = Intent(this , AddAddressActivity::class.java)
            startActivity(i)
        }
    }

//    private fun checkLoginAndRedirect(targetIfLoggedIn: Class<*> = profile::class.java) {
//        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
//        val userId = sharedPref.getLong("userId", -1L)
//
//        println("Stored UserId: $userId") // Debug
//
//        if (userId != -1L) {
//            val dbHelper = DatabaseHelper(this)
//            val exists = dbHelper.isUserExists(userId)
//            println("User Exists in DB: $exists") // Debug
//
//            if (exists) {
//                startActivity(Intent(this, EditProfile::class.java))
//            } else {
//                startActivity(Intent(this, signup::class.java))
//            }
//        } else {
//            startActivity(Intent(this, signup::class.java))
//        }
//    }
//



    // Handle language button clicks
    fun onLanguageButtonClick(view: View) {
        when (view.id) {
            R.id.btnEnglish -> changeAppLanguage("English")
            R.id.btnHindi -> changeAppLanguage("हिंदी")
            R.id.btnTamil -> changeAppLanguage("தமிழ்")
            R.id.btnTelugu -> changeAppLanguage("తెలుగు")
            R.id.btnKannada -> changeAppLanguage("ಕನ್ನಡ")
            R.id.btnUrdu -> changeAppLanguage("اردو")
        }
    }

    // Change the app's language
    private fun changeAppLanguage(language: String) {
        val languageCode = languageMap[language] ?: TranslateLanguage.ENGLISH
        TranslationUtil.saveLanguage(this, languageCode)
        println("Changing app language to: $languageCode")

        // Initialize the translator and wait for it to be ready
        TranslationUtil.initializeTranslator(TranslateLanguage.ENGLISH, languageCode) {
            // Translator is ready, restart the app
            restartApp()
        }
    }

    // Restart the app to apply language changes
    private fun restartApp() {
        val intent = Intent(this, home::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun translateActivityText() {
        val savedLanguageCode = TranslationUtil.getSavedLanguage(this)
        if (savedLanguageCode == TranslateLanguage.ENGLISH) return // No translation needed for English

        // List of TextViews to translate
        val textViewsToTranslate = listOf(
//            findViewById<TextView>(R.id.editProfileTV),
            findViewById<TextView>(R.id.saveAddressTV),
            findViewById<TextView>(R.id.profilebackbtntitle),
            findViewById<TextView>(R.id.languageChangeTV),
            findViewById<TextView>(R.id.accountSettingsTV),
            findViewById<TextView>(R.id.myActivityTV),
            findViewById<TextView>(R.id.reviewTV),
            findViewById<TextView>(R.id.Logout)
        )

        // Translate each TextView
        textViewsToTranslate.forEach { textView ->
            val textToTranslate = textView?.text.toString()
            if (textToTranslate.isNotEmpty()) {
                TranslationUtil.translateText(
                    textToTranslate,
                    targetLanguage = savedLanguageCode, // Use the saved language
                    onSuccess = { translatedText ->
                        runOnUiThread {
                            if (translatedText != null) {
                                textView?.text = translatedText // Update UI on the main thread
                                println("Translated text: $translatedText") // Log the translated text
                            } else {
                                println("Translation returned null for text: $textToTranslate")
                            }
                        }
                    },
                    onFailure = { exception ->
                        println("Translation failed: ${exception.message}")
                    }
                )
            } else {
                println("Empty text encountered for translation")
            }
        }
    }

    private fun applyTranslations() {
        val savedLanguageCode = TranslationUtil.getSavedLanguage(this)
        println("Applying translations for language: $savedLanguageCode") // Log the applied language
        if (savedLanguageCode != TranslateLanguage.ENGLISH) {
            translateActivityText() // Translate UI elements
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                logoutUser()
            }
            .setNegativeButton("No") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .show()
    }

    private fun logoutUser() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, signin::class.java)
        startActivity(intent)
        finish()
    }
}