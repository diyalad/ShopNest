package com.example.shopnest

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.shopnest.databinding.ActivityAddAddressBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale
import com.example.shopnest.R // Import the R class

class AddAddressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAddressBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var settingsClient: SettingsClient
    private lateinit var locationRequest: LocationRequest
    private var userId: Long = -1

    // Register the permissions callback
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted
                checkLocationSettings()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Approximate location access granted
                checkLocationSettings()
            }
            else -> {
                // No location access granted
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FusedLocationProviderClient and SettingsClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        settingsClient = LocationServices.getSettingsClient(this)

        // Initialize LocationRequest
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Handle "Use my location" click
        binding.useMyLocation.setOnClickListener {
            checkLocationPermissions()
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        // Handle "Add Alternate Phone Number" click
        binding.addAlternatePhone.setOnClickListener {
            addAlternatePhoneNumberField()
        }

        // Get user ID from intent
        userId = intent.getLongExtra("USER_ID", -1)
        if (userId == -1L) {
            Toast.makeText(this, "User not identified", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Handle "Save Address" button click
        binding.saveAddressButton.setOnClickListener {
            if (validateForm()) {
                // Form is valid, proceed with saving the address
                saveAddress()
            } else {
                // Form is invalid, show error messages
                Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            // Permissions already granted, check location settings
            checkLocationSettings()
        }
    }

    private fun checkLocationSettings() {
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        val task: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(locationSettingsRequest)

        task.addOnSuccessListener {
            // Location is enabled, fetch the current location
            getCurrentLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location is not enabled, show a dialog to enable it
                try {
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                }
            } else {
                // Location settings are not satisfied
                showLocationSettingsAlert()
            }
        }
    }

    private fun showLocationSettingsAlert() {
        AlertDialog.Builder(this)
            .setTitle("Location Required")
            .setMessage("Please enable location to use this feature.")
            .setPositiveButton("Enable") { _, _ ->
                // Open location settings
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    // Use Geocoder to get address details from latitude and longitude
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses: List<Address>? =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        // Fill the address details automatically
                        fillAddressDetails(address)
                    } else {
                        Toast.makeText(this, "Unable to fetch address details", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fillAddressDetails(address: Address) {
        // Fill the address fields
        binding.pincodeEditText.setText(address.postalCode ?: "")
        binding.stateEditText.setText(address.adminArea ?: "")
        binding.cityEditText.setText(address.locality ?: "")
        binding.houseNoEditText.setText(address.featureName ?: "")
        binding.roadNameEditText.setText(address.thoroughfare ?: "")
    }
    private fun addAlternatePhoneNumberField() {
        val textInputLayout = TextInputLayout(this, null,    com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox
        ).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // Add margin to match the static fields
                bottomMargin = resources.getDimensionPixelSize(R.dimen.margin_bottom)
            }

            // Set the hint
            hint = "Alternate Phone Number"

            // Ensure the box background mode is set to OUTLINE
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE

            // Set the box stroke color and width
            boxStrokeColor = ContextCompat.getColor(this@AddAddressActivity, R.color.box_stroke_color)
            boxStrokeWidth = resources.getDimensionPixelSize(R.dimen.box_stroke_width)

            // Set the box corner radii
            setBoxCornerRadii(
                resources.getDimension(R.dimen.box_corner_radius),
                resources.getDimension(R.dimen.box_corner_radius),
                resources.getDimension(R.dimen.box_corner_radius),
                resources.getDimension(R.dimen.box_corner_radius)
            )

            // Set the hint text color
            setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this@AddAddressActivity, R.color.hint_color)))
        }

        // Create a new TextInputEditText
        val textInputEditText = TextInputEditText(this).apply {
            id = View.generateViewId() // Assign a unique ID
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            hint = "Alternate Phone Number"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }

        // Add the TextInputEditText to the TextInputLayout
        textInputLayout.addView(textInputEditText)

        // Add the TextInputLayout to the container
        binding.alternatePhoneContainer.addView(textInputLayout)
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validate Full Name
        if (binding.fullNameEditText.text.isNullOrEmpty()) {
            binding.fullNameEditText.error = "Full Name is required"
            isValid = false
        } else {
            binding.fullNameEditText.error = null
        }

        // Validate Phone Number
        if (binding.phoneNumberEditText.text.isNullOrEmpty()) {
            binding.phoneNumberEditText.error = "Phone Number is required"
            isValid = false
        } else {
            binding.phoneNumberEditText.error = null
        }

        // Validate Alternate Phone Numbers
        if (!validateAlternatePhoneNumbers()) {
            isValid = false
        }

        // Validate Pincode
        if (binding.pincodeEditText.text.isNullOrEmpty()) {
            binding.pincodeEditText.error = "Pincode is required"
            isValid = false
        } else {
            binding.pincodeEditText.error = null
        }

        // Validate State
        if (binding.stateEditText.text.isNullOrEmpty()) {
            binding.stateEditText.error = "State is required"
            isValid = false
        } else {
            binding.stateEditText.error = null
        }

        // Validate City
        if (binding.cityEditText.text.isNullOrEmpty()) {
            binding.cityEditText.error = "City is required"
            isValid = false
        } else {
            binding.cityEditText.error = null
        }

        // Validate House No., Building Name
        if (binding.houseNoEditText.text.isNullOrEmpty()) {
            binding.houseNoEditText.error = "House No., Building Name is required"
            isValid = false
        } else {
            binding.houseNoEditText.error = null
        }

        // Validate Road Name, Area, Colony
        if (binding.roadNameEditText.text.isNullOrEmpty()) {
            binding.roadNameEditText.error = "Road Name, Area, Colony is required"
            isValid = false
        } else {
            binding.roadNameEditText.error = null
        }

        return isValid
    }

    private fun validateAlternatePhoneNumbers(): Boolean {
        var isValid = true

        for (i in 0 until binding.alternatePhoneContainer.childCount) {
            val child = binding.alternatePhoneContainer.getChildAt(i)
            if (child is TextInputLayout) {
                // Find the TextInputEditText by its ID
                val editText = child.findViewById<TextInputEditText>(child.getChildAt(0).id)
                if (editText.text.isNullOrEmpty()) {
                    editText.error = "Alternate Phone Number is required"
                    isValid = false
                } else {
                    editText.error = null
                }
            }
        }

        return isValid
    }

    private fun saveAddress() {
        // Get the values from the form fields
        val fullName = binding.fullNameEditText.text.toString()
        val phone = binding.phoneNumberEditText.text.toString()
        val pincode = binding.pincodeEditText.text.toString()
        val state = binding.stateEditText.text.toString()
        val city = binding.cityEditText.text.toString()
        val houseNo = binding.houseNoEditText.text.toString()
        val roadName = binding.roadNameEditText.text.toString()

        // Collect alternate phone numbers
        val alternatePhones = mutableListOf<String>()
        for (i in 0 until binding.alternatePhoneContainer.childCount) {
            val child = binding.alternatePhoneContainer.getChildAt(i)
            if (child is TextInputLayout) {
                val editText = child.findViewById<TextInputEditText>(com.google.android.material.R.id.textinput_placeholder)
                val phoneNumber = editText.text.toString()
                if (phoneNumber.isNotEmpty()) {
                    alternatePhones.add(phoneNumber)
                }
            }
        }

        val alternatePhoneString = alternatePhones.joinToString(",")

        // Save the address to the database using the passed user ID
        val dbHelper = DatabaseHelper(this)
        val addressId = dbHelper.insertAddress(
            fullName,
            phone,
            alternatePhoneString,
            pincode,
            state,
            city,
            houseNo,
            roadName,
            userId // Use the passed user ID instead of hardcoded value
        )

        if (addressId != -1L) {
            Toast.makeText(this, "Address saved successfully", Toast.LENGTH_SHORT).show()
            finish() // Close the activity after saving
        } else {
            Toast.makeText(this, "Failed to save address", Toast.LENGTH_SHORT).show()
        }
    }
    companion object {
        private const val REQUEST_CHECK_SETTINGS = 1001
    }
}