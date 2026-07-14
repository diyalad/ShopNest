package com.example.shopnest

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class ReportsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Enable the back arrow
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Display the report information
        displayReportInformation()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_reports, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item1 -> {
                val i = Intent(this , UserReportActivity::class.java)
                startActivity(i)
                true
            }
            R.id.menu_item2 -> {
//                val i = Intent(this , CourseReportActivity::class.java)
//                startActivity(i)
                true
            }
            R.id.menu_item3 -> {
                Toast.makeText(this, "Order History Report Clicked", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_item4 -> {
                Toast.makeText(this, "Order History Report Clicked", Toast.LENGTH_SHORT).show()
                true
            }
            android.R.id.home -> {
                val intent = Intent(this, AdminDashBoard::class.java)
                startActivity(intent)
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun displayReportInformation() {
        // Get the TextView
        val tvReportInfo: TextView = findViewById(R.id.tvReportInfo)

        // Create the information text with bullet points and steps
        val reportInfo = """
            Welcome to the Reports Section!

            Here, you can generate and view various types of reports. Below are the steps to generate a report:

            1. *Select a Report Type*:
               - Choose the type of report you want to generate from the dropdown menu in the top-right corner.

            2. *Set Filters (Optional)*:
               - Apply filters such as date range, category, or user to customize the report.

            3. *Generate the Report*:
               - Click the "Generate" button to create the report based on your selected criteria.

            4. *View or Export the Report*:
               - Once generated, you can view the report on the screen or export it as a PDF/Excel file.

            *Available Reports*:
            - User Report: View details about users.
            - Category Report: View product categories.
            - Order History Report: Track orders and transactions.
            - Product Report: Monitor product sales and performance.
            - Rating Report: Check user ratings and feedback.
                
                 For further assistance, contact the support team.
        """.trimIndent()

        // Set the text in the TextView
        tvReportInfo.text = reportInfo
    }
}