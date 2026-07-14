package com.example.shopnest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shopnest.Model.Address
import com.example.shopnest.databinding.ActivityEditAddressBinding

class EditAddressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditAddressBinding
    private lateinit var dbHelper: DatabaseHelper
    private var addressId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        // Get the address ID from the intent
        addressId = intent.getLongExtra("ADDRESS_ID", -1)

        if (addressId == -1L) {
            Toast.makeText(this, "Invalid address", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Fetch the address details from the database
        val address = dbHelper.getAddressById(addressId)
        if (address != null) {
            // Populate the form fields with the address details
            binding.fullNameEditText.setText(address.fullName)
            binding.phoneNumberEditText.setText(address.phone)
            binding.pincodeEditText.setText(address.pincode)
            binding.stateEditText.setText(address.state)
            binding.cityEditText.setText(address.city)
            binding.houseNoEditText.setText(address.houseNo)
            binding.roadNameEditText.setText(address.roadName)
        } else {
            Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Handle "Save Changes" button click
        binding.saveAddressButton.setOnClickListener {
            if (validateForm()) {
                // Fetch the address again to ensure it's not null
                val address = dbHelper.getAddressById(addressId)
                if (address != null) {
                    // Update the address in the database
                    val fullName = binding.fullNameEditText.text.toString()
                    val phone = binding.phoneNumberEditText.text.toString()
                    val pincode = binding.pincodeEditText.text.toString()
                    val state = binding.stateEditText.text.toString()
                    val city = binding.cityEditText.text.toString()
                    val houseNo = binding.houseNoEditText.text.toString()
                    val roadName = binding.roadNameEditText.text.toString()

                    val updatedAddress = Address(
                        addressId,
                        fullName,
                        phone,
                        "", // Alternate phone (not editable in this example)
                        pincode,
                        state,
                        city,
                        houseNo,
                        roadName,
                        address.userId // Safe access since address is not null
                    )

                    val result = dbHelper.updateAddress(updatedAddress)
                    if (result > 0) {
                        Toast.makeText(this, "Address updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to update address", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show()
            }
        }
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
}