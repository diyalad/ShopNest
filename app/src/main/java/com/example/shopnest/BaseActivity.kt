package com.example.shopnest

import TranslationUtil
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.nl.translate.TranslateLanguage

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply the saved language
        val savedLanguage = TranslationUtil.getSavedLanguage(this)
        println("Initializing translator with language: $savedLanguage") // Log the saved language

        // Initialize translators for all supported languages
        TranslationUtil.initializeTranslator(TranslateLanguage.ENGLISH, savedLanguage)
        TranslationUtil.initializeTranslator(TranslateLanguage.TAMIL, savedLanguage)
        TranslationUtil.initializeTranslator(TranslateLanguage.TELUGU, savedLanguage)
        TranslationUtil.initializeTranslator(TranslateLanguage.KANNADA, savedLanguage)
        TranslationUtil.initializeTranslator(TranslateLanguage.URDU, savedLanguage)
        TranslationUtil.initializeTranslator(TranslateLanguage.HINDI, savedLanguage)

        // Automatically translate all text in the activity
        translateActivityText()
    }

    // Abstract method to translate text in the activity
    abstract fun translateActivityText()

    override fun onDestroy() {
        super.onDestroy()
        // Close the translator when the activity is destroyed
        TranslationUtil.closeTranslator()
    }
    protected val sharedPrefs by lazy {
        getSharedPreferences("user_prefs", MODE_PRIVATE)
    }

    protected fun saveUserId(userId: Long) {
        sharedPrefs.edit().putLong("user_id", userId).apply()
    }

    protected fun getUserId(): Long {
        return sharedPrefs.getLong("user_id", -1L)
    }

    protected fun clearSession() {
        sharedPrefs.edit().clear().apply()
    }
}