package com.example.shopnest

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.example.shopnest.Model.Profile
import com.google.android.material.tabs.TabLayout
import java.io.File
import java.io.FileOutputStream
import java.util.*

class EditProfile : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewFlipper: ViewFlipper
    private lateinit var backButton: ImageView
    private lateinit var profileImage: ImageView
    private lateinit var dob: EditText
    private lateinit var btnSave: Button
    private lateinit var deleteAccountTxt: TextView
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Views
        tabLayout = findViewById(R.id.tabLayout)
        viewFlipper = findViewById(R.id.viewFlipper)
        backButton = findViewById(R.id.backButton)
        profileImage = findViewById(R.id.profileImage)
        dob = findViewById(R.id.dob)
        btnSave = findViewById(R.id.btnSave)
        deleteAccountTxt = findViewById(R.id.DeleteAccountTxt)

        dbHelper = DatabaseHelper(this)
        btnSave.visibility = View.VISIBLE

        // Get userId from intent
        userId = intent.getLongExtra("USER_ID", -1)

        if (userId == -1L) {
            Log.e("EditProfile", "Invalid user ID received")
            Toast.makeText(this, "Invalid User", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Verify user existence
        if (!dbHelper.isUserExists(userId)) {
            Log.e("EditProfile", "User does not exist")
            Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load and display user data
        val user = dbHelper.getUserById(userId)
        user?.let {
            findViewById<EditText>(R.id.fullName).setText(it.name)
            findViewById<EditText>(R.id.phone).setText(it.phone)
            findViewById<EditText>(R.id.email).setText(it.email)
        }

        // Setup dropdown spinners
        setupDropdown(R.id.genderSpinner, arrayOf("Select Gender", "Male", "Female", "Other"))
        setupDropdown(R.id.maritalStatusSpinner, arrayOf("Select Marital Status", "Single", "Married", "Divorced", "Widowed"))
        setupDropdown(R.id.educationSpinner, arrayOf("Select Education", "High School", "Bachelor's", "Master's", "PhD"))
        setupDropdown(R.id.incomeSpinner, arrayOf("Select Income Range", "Below 20k", "20k-50k", "50k-100k", "Above 100k"))

        // Load and display profile data
        val profile = dbHelper.getProfileByUserId(userId)
        profile?.let {
            findViewById<EditText>(R.id.fullName).setText(it.fullName)
            findViewById<EditText>(R.id.phone).setText(it.phone)
            findViewById<EditText>(R.id.email).setText(it.email)
            findViewById<EditText>(R.id.pincode).setText(it.pincode)
            findViewById<EditText>(R.id.city).setText(it.city)
            findViewById<EditText>(R.id.state).setText(it.state)
            dob.setText(it.dob)

            // Load profile image if available
            it.profileImagePath?.let { imagePath ->
                val bitmap = BitmapFactory.decodeFile(imagePath)
                if (bitmap != null) {
                    profileImage.setImageBitmap(bitmap)
                } else {
                    Log.e("EditProfile", "Failed to decode image from path: $imagePath")
                    // Optionally, set a default image
                    profileImage.setImageResource(R.drawable.default_profile_image)
                }
            }

            // Set spinner selections
            setSpinnerSelection(R.id.genderSpinner, it.gender)
            setSpinnerSelection(R.id.maritalStatusSpinner, it.maritalStatus)
            setSpinnerSelection(R.id.educationSpinner, it.education)
            setSpinnerSelection(R.id.incomeSpinner, it.income)
        }

        // Setup Tabs
        tabLayout.addTab(tabLayout.newTab().setText("Primary Info"))
        tabLayout.addTab(tabLayout.newTab().setText("Other Info"))
        tabLayout.addTab(tabLayout.newTab().setText("Delete"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        viewFlipper.displayedChild = 0
                        btnSave.visibility = View.VISIBLE
                    }
                    1 -> {
                        viewFlipper.displayedChild = 1
                        btnSave.visibility = View.VISIBLE
                    }
                    2 -> {
                        viewFlipper.displayedChild = 2
                        btnSave.visibility = View.GONE
                        showDeleteConfirmationDialog()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Save Button
        btnSave.setOnClickListener {
            // Double-check user existence
            if (!dbHelper.isUserExists(userId)) {
                Toast.makeText(this, "User no longer exists", Toast.LENGTH_SHORT).show()
                finish()
                return@setOnClickListener
            }

            val fullName = findViewById<EditText>(R.id.fullName).text.toString()
            val phone = findViewById<EditText>(R.id.phone).text.toString()
            val email = findViewById<EditText>(R.id.email).text.toString()
            val gender = findViewById<Spinner>(R.id.genderSpinner).selectedItem.toString()
            val pincode = findViewById<EditText>(R.id.pincode).text.toString()
            val city = findViewById<EditText>(R.id.city).text.toString()
            val state = findViewById<EditText>(R.id.state).text.toString()
            val dobText = dob.text.toString()
            val maritalStatus = findViewById<Spinner>(R.id.maritalStatusSpinner).selectedItem.toString()
            val education = findViewById<Spinner>(R.id.educationSpinner).selectedItem.toString()
            val income = findViewById<Spinner>(R.id.incomeSpinner).selectedItem.toString()

            // Save the image path if the drawable is not null
            val profileImageDrawable = profileImage.drawable
            val profileImagePath: String? = if (profileImageDrawable != null) {
                saveImageToInternalStorage(profileImageDrawable.toBitmap())
            } else {
                // Use the existing image path if the drawable is null
                profile?.profileImagePath
            }

            // Update user basic info
            dbHelper.updateUser(userId, fullName, email, phone)
            Log.d("EditProfile", "User data updated for user ID: $userId")

            // Check if profile exists
            val profileId = dbHelper.getProfileIdByUserId(userId)
            if (profileId != -1L) {
                dbHelper.updateProfile(
                    profileId, fullName, phone, email, gender, pincode, city, state, dobText,
                    maritalStatus, education, income, profileImagePath
                )
                Log.d("EditProfile", "Profile updated for user ID: $userId")
            } else {
                dbHelper.addProfile(
                    userId, fullName, phone, email, gender, pincode, city, state, dobText,
                    maritalStatus, education, income, profileImagePath
                )
                Log.d("EditProfile", "Profile inserted for user ID: $userId")
            }

            Toast.makeText(this, "Profile Saved Successfully", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Delete Account Text Click
        deleteAccountTxt.setOnClickListener {
            val intent = Intent(this, DeleteAccount::class.java)
            startActivity(intent)
        }

        // Back Button
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Profile Image Click
        profileImage.setOnClickListener {
            openGallery()
        }

        // DOB Click
        dob.setOnClickListener {
            showDatePicker()
        }
    }

    // Show Delete Confirmation Dialog
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Profile")
            .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteProfile()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Delete Profile
    private fun deleteProfile() {
        val rowsDeleted = dbHelper.deleteProfile(userId)
        if (rowsDeleted > 0) {
            Toast.makeText(this, "Profile deleted successfully", Toast.LENGTH_SHORT).show()
            finish() // Close the activity after deletion
        } else {
            Toast.makeText(this, "Failed to delete profile", Toast.LENGTH_SHORT).show()
        }
    }

    // Setup Spinner Dropdown
    private fun setupDropdown(spinnerId: Int, options: Array<String>) {
        val spinner: Spinner = findViewById(spinnerId)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        spinner.adapter = adapter
        spinner.setSelection(0)
    }

    // Set Spinner Selection
    private fun setSpinnerSelection(spinnerId: Int, value: String) {
        val spinner: Spinner = findViewById(spinnerId)
        val adapter = spinner.adapter as? ArrayAdapter<String>
        if (adapter != null) {
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i) == value) {
                    spinner.setSelection(i)
                    break
                }
            }
        } else {
            Log.e("EditProfile", "Spinner adapter is null or not an ArrayAdapter<String>")
        }
    }

    // Gallery Launcher
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val selectedImageUri: Uri? = result.data?.data
            selectedImageUri?.let {
                profileImage.setImageURI(it)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    // Date Picker
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            dob.setText(date)
        }, year, month, day)

        datePicker.show()
    }

    // Save Image to Internal Storage
    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val wrapper = ContextWrapper(applicationContext)
        val file = wrapper.getDir("images", Context.MODE_PRIVATE)
        file.mkdirs()
        val filePath = File(file, "${UUID.randomUUID()}.jpg")
        val stream = FileOutputStream(filePath)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.flush()
        stream.close()
        return filePath.absolutePath
    }
}