package com.example.shopnest

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shopnest.Model.Address
import com.example.shopnest.databinding.ActivitySelectAddressBinding

class SelectAddressActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectAddressBinding
    private lateinit var dbHelper: DatabaseHelper
    private val addresses = mutableListOf<Address>()
    private val radioButtons = mutableListOf<RadioButton>()
    private var userId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeDatabase()
        checkUserId()
        setupButtons()
    }

    private fun initializeDatabase() {
        dbHelper = DatabaseHelper(this)
    }

    private fun checkUserId() {
        userId = intent.getLongExtra("USER_ID", -1).also {
            if (it == -1L) {
                Toast.makeText(this, "User not identified", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupButtons() {
        binding.addNewAddressButton.setOnClickListener {
            val intent = Intent(this, AddAddressActivity::class.java).apply {
                putExtra("USER_ID", userId) // Pass the user ID
            }
            startActivity(intent)
        }

        binding.deliverHereButton.setOnClickListener {
            getSelectedAddress()?.let { address ->
                setResult(RESULT_OK, Intent().apply {
                    putExtra("SELECTED_ADDRESS", formatAddress(address))
                })
                finish()
            } ?: Toast.makeText(this, "Please select an address", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshAddressList()
    }

    private fun refreshAddressList() {
        clearExistingViews()
        loadAddressesFromDB()
        if (addresses.isEmpty()) {
            showNoAddressMessage()
        } else {
            displayAddresses()
        }
    }

    private fun clearExistingViews() {
        binding.addressRadioGroup.removeAllViews()
        radioButtons.clear()
        addresses.clear()
    }

    private fun loadAddressesFromDB() {
        addresses.addAll(dbHelper.getAddressesByUserId(userId))
    }

    private fun showNoAddressMessage() {
        Toast.makeText(this, "No addresses found", Toast.LENGTH_SHORT).show()
    }

    private fun displayAddresses() {
        addresses.forEach { address ->
            val container = createAddressContainer()
            val (radioButton, editButton) = createAddressViews(address)

            container.addView(radioButton)
            container.addView(editButton)
            binding.addressRadioGroup.addView(container)
        }
    }

    private fun createAddressContainer(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(dpToPx(4), dpToPx(8), dpToPx(4), dpToPx(8))
        }
    }

    private fun createAddressViews(address: Address): Pair<RadioButton, Button> {
        val radioButton = RadioButton(this).apply {
            id = View.generateViewId()
            text = formatAddress(address)
            textSize = 14f
            setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 1f
                setMargins(0, 0, dpToPx(8), 0)
            }
            setOnClickListener { uncheckOtherRadios(this) }
        }

        val editButton = Button(this).apply {
            text = "Edit"
            textSize = 13f
            setTextColor(resources.getColor(R.color.deep_blue))
            background = resources.getDrawable(R.drawable.edit_button_background)
            layoutParams = LinearLayout.LayoutParams(dpToPx(75), dpToPx(40)).apply {
                setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            }
            setOnClickListener {
                startActivity(Intent(this@SelectAddressActivity, EditAddressActivity::class.java).apply {
                    putExtra("ADDRESS_ID", address.addressId)
                    putExtra("USER_ID", userId)
                })
            }
        }

        radioButtons.add(radioButton)
        return Pair(radioButton, editButton)
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private fun uncheckOtherRadios(selectedRadio: RadioButton) {
        radioButtons.forEach { if (it != selectedRadio) it.isChecked = false }
    }

    private fun getSelectedAddress(): Address? {
        radioButtons.forEachIndexed { index, rb ->
            if (rb.isChecked) return addresses[index]
        }
        return null
    }

    private fun formatAddress(address: Address): String {
        return """
            ${address.fullName}
            ${address.houseNo}, ${address.roadName}
            ${address.city}, ${address.state} - ${address.pincode}
            Phone: ${address.phone}
            ${address.alternatePhone?.takeIf { it.isNotBlank() }?.let { "Alternate: $it" } ?: ""}
        """.trimIndent()
    }
}