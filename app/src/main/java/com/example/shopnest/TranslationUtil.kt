import android.content.Context
import android.content.SharedPreferences
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

object TranslationUtil {

    private var translator: Translator? = null
    private var currentTargetLanguage: String? = null
    private const val PREFS_NAME = "LanguagePrefs"
    private const val KEY_LANGUAGE_CODE = "languageCode"

    // Save the selected language to SharedPreferences
    fun saveLanguage(context: Context, languageCode: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE_CODE, languageCode).apply()
        println("Language saved: $languageCode") // Log the saved language
    }

    // Get the saved language from SharedPreferences
    fun getSavedLanguage(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE_CODE, TranslateLanguage.ENGLISH) ?: TranslateLanguage.ENGLISH
    }

    fun initializeTranslator(sourceLanguage: String, targetLanguage: String, onReady: (() -> Unit)? = null) {
        if (currentTargetLanguage == targetLanguage && translator != null) {
            println("Translator already initialized for language: $targetLanguage")
            onReady?.invoke() // Notify that the translator is ready
            return
        }

        println("Initializing translator: $sourceLanguage -> $targetLanguage")

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()

        translator = Translation.getClient(options)
        currentTargetLanguage = targetLanguage

        // Download the language model if needed
        translator?.downloadModelIfNeeded()
            ?.addOnSuccessListener {
                println("Language model downloaded successfully: $targetLanguage")
                onReady?.invoke() // Notify that the translator is ready
            }
            ?.addOnFailureListener { exception ->
                println("Failed to download language model: ${exception.message}")
            }
    }
    // Translate a string
    fun translateText(
        text: String,
        targetLanguage: String = TranslateLanguage.ENGLISH, // Default to English
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Initialize the translator if not already initialized or if the target language has changed
        if (translator == null || currentTargetLanguage != targetLanguage) {
            initializeTranslator(TranslateLanguage.ENGLISH, targetLanguage)
        }

        translator?.translate(text)
            ?.addOnSuccessListener { translatedText ->
                onSuccess(translatedText)
            }
            ?.addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Close the translator when no longer needed
    fun closeTranslator() {
        translator?.close()
        translator = null
        currentTargetLanguage = null
    }
}