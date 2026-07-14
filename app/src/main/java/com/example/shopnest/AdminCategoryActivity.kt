package com.example.shopnest

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shopnest.databinding.ActivityAdminCategoryBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.appbar.MaterialToolbar
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class AdminCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminCategoryBinding
    private lateinit var tilCategoryName: TextInputLayout
    private lateinit var etCategoryName: TextInputEditText
    private lateinit var btnSelectImage: MaterialButton
    private lateinit var ivCategoryImage: ShapeableImageView
    private lateinit var btnSaveCategory: MaterialButton
    private lateinit var toolbar: MaterialToolbar

    private var selectedImageBitmap: Bitmap? = null
    private lateinit var dbHelper: DatabaseHelper

    companion object {
        private const val TAG = "AdminCategoryActivity"
        private const val REQUEST_IMAGE_PICK = 100
        private val DEFAULT_CATEGORY_IMAGE_RES_ID = R.drawable.ic_add_photo
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "Activity created")

        initViews()
        setupToolbar()
        setupClickListeners()
    }

    private fun initViews() {
        toolbar = binding.toolbar
        tilCategoryName = binding.tilCategoryName
        etCategoryName = binding.etCategoryName
        btnSelectImage = binding.btnSelectImage
        ivCategoryImage = binding.ivCategoryImage
        btnSaveCategory = binding.btnSaveCategory
        dbHelper = DatabaseHelper(this)

        // Set default image
        ivCategoryImage.setImageResource(DEFAULT_CATEGORY_IMAGE_RES_ID)
        Log.d(TAG, "Views initialized")
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Add New Category"
        }
        toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Back button pressed")
            onBackPressed()
        }
        Log.d(TAG, "Toolbar setup completed")
    }

    private fun setupClickListeners() {
        btnSelectImage.setOnClickListener {
            Log.d(TAG, "Select image button clicked")
            selectImageFromGallery()
        }

        btnSaveCategory.setOnClickListener {
            Log.d(TAG, "Save category button clicked")
           // saveCategory()
        }
        Log.d(TAG, "Click listeners setup completed")
    }

    private fun selectImageFromGallery() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
            Log.d(TAG, "Image picker intent launched")
        } catch (e: Exception) {
            Log.e(TAG, "Error launching image picker", e)
            Toast.makeText(this, "Error opening gallery", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK -> {
                data?.data?.let { uri ->
                    try {
                        Log.d(TAG, "Image selected from gallery: $uri")
                        selectedImageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        ivCategoryImage.setImageBitmap(selectedImageBitmap)
                        Log.d(TAG, "Image loaded successfully into ImageView")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading selected image", e)
                        Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
                        resetImageToDefault()
                    }
                } ?: run {
                    Log.w(TAG, "No image URI returned from picker")
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                }
            }
            resultCode == Activity.RESULT_CANCELED -> {
                Log.d(TAG, "Image selection cancelled by user")
            }
            else -> {
                Log.w(TAG, "Unknown activity result: requestCode=$requestCode, resultCode=$resultCode")
            }
        }
    }

//    private fun saveCategory() {
//        val categoryName = etCategoryName.text.toString().trim()
//
//        if (categoryName.isEmpty()) {
//            tilCategoryName.error = "Category name is required"
//            return
//        }
//
//        if (selectedImageBitmap == null) {
//            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        try {
//            // Save the bitmap to a file and get the file path
//            val imagePath = saveBitmapToFile(selectedImageBitmap!!)
//
//            // Insert into database
//            val id = dbHelper.insertCategory(categoryName, imagePath)
//
//            if (id != -1L) {
//                Toast.makeText(this, "Category saved successfully", Toast.LENGTH_SHORT).show()
//                clearForm()
//            } else {
//                Toast.makeText(this, "Failed to save category", Toast.LENGTH_SHORT).show()
//            }
//        } catch (e: Exception) {
//            Toast.makeText(this, "Error saving category: ${e.message}", Toast.LENGTH_SHORT).show()
//            Log.e(TAG, "Error saving category", e)
//        }
//    }

    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val filename = "${System.currentTimeMillis()}_category_image.png"
        val file = File(cacheDir, filename)
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()
        return file.absolutePath
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun validateInputs(categoryName: String): Boolean {
        tilCategoryName.error = null

        if (categoryName.isEmpty()) {
            Log.w(TAG, "Validation failed: Category name is empty")
            tilCategoryName.error = "Category name is required"
            return false
        }

        if (dbHelper.checkCategoryExists(categoryName)) {
            Log.w(TAG, "Validation failed: Category '$categoryName' already exists")
            tilCategoryName.error = "Category already exists"
            return false
        }

        return true
    }

    private fun showSuccessAndResetForm() {
        Toast.makeText(this, "Category saved successfully", Toast.LENGTH_SHORT).show()
        clearForm()
    }

    private fun clearForm() {
        Log.d(TAG, "Clearing form inputs")
        etCategoryName.text?.clear()
        resetImageToDefault()
    }

    private fun resetImageToDefault() {
        ivCategoryImage.setImageResource(DEFAULT_CATEGORY_IMAGE_RES_ID)
        selectedImageBitmap = null
    }

    override fun onDestroy() {
        Log.d(TAG, "Activity destroyed")
        try {
            dbHelper.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing database helper", e)
        }
        super.onDestroy()
    }
}