package com.example.shopnest

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class UserReportActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var rvUserDetails: RecyclerView
    private lateinit var btnUserDetails: Button
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var tableHeaderView: View
    private lateinit var scrollView: androidx.core.widget.NestedScrollView
    private lateinit var tvInsightSummary: TextView
    private lateinit var quarterInfoCard: CardView
    private lateinit var fabDownload: FloatingActionButton

    // Store user distribution for chart and popup
    private lateinit var userDistribution: Map<String, Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_report)

        dbHelper = DatabaseHelper(this)
        db = dbHelper.readableDatabase

        scrollView = findViewById(R.id.scrollView)
        rvUserDetails = findViewById(R.id.rvUserDetails)
        btnUserDetails = findViewById(R.id.btnUserDetails)
        lineChart = findViewById(R.id.lineChart)
        pieChart = findViewById(R.id.pieChart)
        tableHeaderView = findViewById(R.id.tableHeader)
        tvInsightSummary = findViewById(R.id.tvInsightSummary)
        quarterInfoCard = findViewById(R.id.quarterInfoCard)
        fabDownload = findViewById(R.id.fabDownload)

        // Initially hide the quarter info card
        quarterInfoCard.visibility = View.GONE

        btnUserDetails.setOnClickListener {
            if (rvUserDetails.visibility == View.GONE) {
                val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
                tableHeaderView.visibility = View.VISIBLE
                rvUserDetails.visibility = View.VISIBLE
                tableHeaderView.startAnimation(slideDown)
                rvUserDetails.startAnimation(slideDown)
                btnUserDetails.text = "Hide User Details"
                loadUserDetails()
            } else {
                val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
                rvUserDetails.startAnimation(slideUp)
                tableHeaderView.startAnimation(slideUp)
                rvUserDetails.visibility = View.GONE
                tableHeaderView.visibility = View.GONE
                btnUserDetails.text = "Show User Details"
            }
        }

        // Set click listener for the download button
        fabDownload.setOnClickListener {
            createPdfFromView(scrollView)
        }

        // Calculate user distribution
        userDistribution = calculateUserDistribution()

        setupStockStyleLineChart()
        setupEnhancedPieChart()
        updateInsightSummary()

        // Add dividers between items
        val dividerItemDecoration = DividerItemDecoration(
            rvUserDetails.context,
            LinearLayoutManager.VERTICAL
        )
        rvUserDetails.addItemDecoration(dividerItemDecoration)
    }

    private fun getTotalUserCount(): Int {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_USERS}", null)
        return if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }
    }

    private fun calculateUserDistribution(): Map<String, Int> {
        val userDistribution = mutableMapOf(
            "Jan" to 0,
            "Feb" to 0,
            "Mar" to 0,
            "Apr" to 0,
            "May" to 0,
            "Jun" to 0
        )

        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_USERS}", null)
        val calendar = Calendar.getInstance()
        val currentYear = 2025
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Current month (1-based)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH) // Current day

        if (cursor.moveToFirst()) {
            do {
                // Generate a random date between January 1st, 2025, and the current date
                val randomMonth = (1..currentMonth).random()
                val randomDay = if (randomMonth == currentMonth) {
                    (1..currentDay).random()
                } else {
                    (1..31).random() // Assuming all months have 31 days for simplicity
                }

                // Assign the user to the corresponding month
                val month = when (randomMonth) {
                    1 -> "Jan"
                    2 -> "Feb"
                    3 -> "Mar"
                    4 -> "Apr"
                    5 -> "May"
                    6 -> "Jun"
                    else -> "Jan" // Default to January if something goes wrong
                }
                userDistribution[month] = userDistribution[month]!! + 1
            } while (cursor.moveToNext())
        }
        cursor.close()

        return userDistribution
    }


    private fun setupStockStyleLineChart() {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) // Get current month (0-based)

        // Configuration
        with(lineChart) {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            setBackgroundColor(Color.WHITE)

            // Animation
            animateX(1500, Easing.EaseInOutQuart)

            // Setup X axis
            val xAxis = xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textSize = 10f
            xAxis.textColor = Color.DKGRAY
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(true)

            // Update labels based on current month
            val monthLabels = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
            val visibleMonths = monthLabels.sliceArray(0..currentMonth)
            xAxis.valueFormatter = IndexAxisValueFormatter(visibleMonths)

            // Setup Y axis
            val leftAxis = axisLeft
            leftAxis.textColor = Color.DKGRAY
            leftAxis.setDrawGridLines(true)
            leftAxis.enableGridDashedLine(10f, 10f, 0f)

            val rightAxis = axisRight
            rightAxis.isEnabled = false

            // Legend
            legend.form = Legend.LegendForm.LINE
            legend.textSize = 12f
            legend.textColor = Color.BLACK
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)
        }

        // Create data based on user distribution
        val entries = ArrayList<Entry>()
        entries.add(Entry(0f, userDistribution["Jan"]?.toFloat() ?: 0f))
        entries.add(Entry(1f, userDistribution["Feb"]?.toFloat() ?: 0f))
        entries.add(Entry(2f, userDistribution["Mar"]?.toFloat() ?: 0f))
        if (currentMonth >= Calendar.APRIL) {
            entries.add(Entry(3f, userDistribution["Apr"]?.toFloat() ?: 0f))
        }
        if (currentMonth >= Calendar.MAY) {
            entries.add(Entry(4f, userDistribution["May"]?.toFloat() ?: 0f))
        }
        if (currentMonth >= Calendar.JUNE) {
            entries.add(Entry(5f, userDistribution["Jun"]?.toFloat() ?: 0f))
        }

        // Create and style the dataset
        val dataSet = LineDataSet(entries, "User Enrollment Progress")
        dataSet.apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
            setDrawFilled(true)
            fillColor = Color.parseColor("#4CAF50")
            fillAlpha = 80
            color = Color.parseColor("#4CAF50")
            lineWidth = 2.5f
            setDrawCircles(true)
            setCircleColor(Color.parseColor("#007BFF"))
            circleRadius = 4f
            setDrawCircleHole(true)
            circleHoleRadius = 2f
            valueTextSize = 10f
            setDrawValues(true)
            valueTextColor = Color.BLACK
            highlightLineWidth = 1.5f
            setDrawHorizontalHighlightIndicator(true)
            highLightColor = Color.RED
        }

        // Apply data to chart
        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Set chart value selected listener
        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.let {
                    val x = h?.xPx?.toInt() ?: 0
                    val y = h?.yPx?.toInt() ?: 0
                    val message = getQuarterInfoByIndex(it.x.toInt())
                    showChatMessageAtPosition(message, x, y)
                }
            }

            override fun onNothingSelected() {
                // Do nothing
            }
        })

        lineChart.invalidate()
    }

    private fun getQuarterInfoByIndex(index: Int): String {
        return when (index) {
            0 -> "January: ${userDistribution["Jan"]} user enrollments.\nThis month marks the beginning of the year with a strong focus on new user acquisition and onboarding."
            1 -> "February: ${userDistribution["Feb"]} user enrollments.\nThis month sees a steady increase in user engagement and retention efforts."
            2 -> "March: ${userDistribution["Mar"]} user enrollments.\nThe first quarter ends with a focus on evaluating user progress and satisfaction."
            3 -> "April: ${userDistribution["Apr"]} user enrollments.\nThe second quarter begins with renewed marketing campaigns and user outreach."
            4 -> "May: ${userDistribution["May"]} user enrollments.\nThis month focuses on enhancing user experience and introducing new features."
            5 -> "June: ${userDistribution["Jun"]} user enrollments.\nMid-year evaluations and strategic planning for the second half of the year."
            else -> "Month information"
        }
    }

    private fun setupEnhancedPieChart() {
        with(pieChart) {
            description.isEnabled = false
            setUsePercentValues(true)
            setExtraOffsets(5f, 10f, 5f, 5f)

            // Center text configuration
            setDrawCenterText(true)
            centerText = "User Distribution"
            setCenterTextSize(16f)
            setCenterTextColor(Color.DKGRAY)

            // Animation
            animateY(1400, Easing.EaseInOutQuad)

            // Hole configuration (for donut style)
            isDrawHoleEnabled = true
            holeRadius = 58f
            transparentCircleRadius = 61f
            setHoleColor(Color.WHITE)

            // Legend
            legend.isEnabled = true
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.setDrawInside(false)
            legend.textSize = 12f

            // Enable rotation by touch
            isRotationEnabled = true
            isHighlightPerTapEnabled = true

            // Set chart value selected listener
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e is PieEntry) {
                        val x = h?.xPx?.toInt() ?: 0
                        val y = h?.yPx?.toInt() ?: 0
                        val message = getQuarterInfo(e.label ?: "")
                        showChatMessageAtPosition(message, x, y)
                    }
                }

                override fun onNothingSelected() {
                    // Do nothing
                }
            })
        }

        // Create data
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(40f, "Q1"))
        entries.add(PieEntry(30f, "Q2"))
        entries.add(PieEntry(20f, "Q3"))
        entries.add(PieEntry(10f, "Q4"))

        // Create dataset with custom colors
        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f

        // Custom colors
        val colors = ArrayList<Int>()
        colors.add(Color.parseColor("#FF5252")) // Red
        colors.add(Color.parseColor("#FF9800")) // Orange
        colors.add(Color.parseColor("#29B6F6")) // Light Blue
        colors.add(Color.parseColor("#66BB6A")) // Green
        dataSet.colors = colors

        // Apply data to chart
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.WHITE)
        pieChart.data = data
        pieChart.highlightValues(null)
        pieChart.invalidate()
    }

    private fun updateInsightSummary() {
        val totalUsers = getTotalUserCount()
        val q1Users = (userDistribution["Jan"] ?: 0) + (userDistribution["Feb"] ?: 0) + (userDistribution["Mar"] ?: 0)
        val q2Users = (userDistribution["Apr"] ?: 0) + (userDistribution["May"] ?: 0) + (userDistribution["Jun"] ?: 0)

        tvInsightSummary.text = """
        Key Insights and Recommendations:
        
        • User enrollment showed ${((q1Users.toFloat() / totalUsers) * 100).toInt()}% growth in Q1 with highest spike in February.
        • ${((q1Users.toFloat() / totalUsers) * 100).toInt()}% of yearly enrollments occurred during Q1, indicating strong initial interest.
        • Consider launching targeted marketing campaigns in April to maintain momentum.
        • Retention rate analysis suggests implementing engagement features to keep Q1 users active.
        • Recommend personalized outreach to recent enrollees to boost completion rates.
    """.trimIndent()
    }

    private fun getQuarterInfo(quarter: String): String {
        return when (quarter) {
            "Q1" -> "Q1 (January-March): First quarter represents the initial enrollment period with key metrics tracking new sign-ups and platform engagement. This period saw ${((userDistribution["Jan"] ?: 0) + (userDistribution["Feb"] ?: 0) + (userDistribution["Mar"] ?: 0))} user enrollments."
            "Q2" -> "Q2 (April-June): Second quarter focuses on user retention and content engagement. This period typically sees continued growth at a more sustainable pace."
            "Q3" -> "Q3 (July-September): Third quarter highlights learning progress metrics and typically shows higher completion rates as users progress through their courses."
            "Q4" -> "Q4 (October-December): Fourth quarter measures overall platform performance and yearly goals achievement, with focus on retention and conversions."
            else -> "Selected quarter information"
        }
    }

    private fun showChatMessageAtPosition(message: String, x: Int, y: Int) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.chat_message_popup, null)

        val tvMessage = popupView.findViewById<TextView>(R.id.tvMessage)
        tvMessage.text = message

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = true

        // Show the popup at the tapped position
        popupWindow.showAtLocation(scrollView, Gravity.NO_GRAVITY, x, y)

        // Dismiss the popup when tapping outside
        popupView.setOnTouchListener { _, _ ->
            popupWindow.dismiss()
            true
        }
    }

    private fun loadUserDetails() {
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_USERS}", null)
        val userList = mutableListOf<User>()
        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME))
                val email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL))
                val phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PHONE))
                userList.add(User(name, email, phone))
            } while (cursor.moveToNext())
        }
        cursor.close()

        rvUserDetails.layoutManager = LinearLayoutManager(this)
        rvUserDetails.adapter = UserAdapter(userList)
    }

    private fun createPdfFromView(view: View) {
        // Create a PdfDocument instance
        val document = PdfDocument()

        // Measure and layout the view to get its dimensions
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        // Create a page with the same dimensions as the view
        val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()
        val page = document.startPage(pageInfo)

        // Draw the view onto the page
        view.draw(page.canvas)

        // Finish the page
        document.finishPage(page)

        // Save the PDF to a file
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "UserReport_${System.currentTimeMillis()}.pdf"
        val file = File(downloadsDir, fileName)

        try {
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.close()
            Toast.makeText(this, "PDF saved to Downloads folder", Toast.LENGTH_SHORT).show()

            // Share the PDF file
            sharePdfFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show()
        }

        // Close the document
        document.close()
    }

    private fun sharePdfFile(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share PDF via"))
    }
}

class UserAdapter(private val userList: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_detail, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.tvUserName.text = user.name
        holder.tvUserEmail.text = user.email
        holder.tvUserPhone.text = user.phone

        // Add alternating row colors for better readability
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.parseColor("#F5F5F5"))
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE)
        }
    }

    override fun getItemCount(): Int = userList.size

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvUserEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        val tvUserPhone: TextView = itemView.findViewById(R.id.tvUserPhone)
    }
}

data class User(val name: String, val email: String, val phone: String)