package com.example.teduproject

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class Report : AppCompatActivity() {

    data class DayXP(
        @SerializedName("day") val day: String,
        @SerializedName("xp") val xp: Float
    )

    data class XPData(
        @SerializedName("lineChartData") val lineChartData: List<DayXP>,
        @SerializedName("totalXP") val totalXP: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // Setup Bottom Navigation
        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)

        // Setup Line Chart
        setupLineChart()
    }

    private fun setupLineChart() {
        val lineChart = findViewById<LineChart>(R.id.lineChart)

        // JSON data
        val jsonData = """
           {
               "lineChartData": [
                   { "day": "Sen", "xp": 60 },
                   { "day": "Sel", "xp": 80 },
                   { "day": "Rab", "xp": 140 },
                   { "day": "Kam", "xp": 100 },
                   { "day": "Jum", "xp": 120 },
                   { "day": "Sab", "xp": 90 },
                   { "day": "Min", "xp": 150 }
               ],
               "totalXP": 834
           }
        """

        // Parse JSON
        val gson = Gson()
        val xpData = gson.fromJson(jsonData, XPData::class.java)

        // Create entries for the chart
        val entries = xpData.lineChartData.mapIndexed { index, dayXP ->
            Entry(index.toFloat(), dayXP.xp)
        }

        // Create LineDataSet
        val dataSet = LineDataSet(entries, "XP minggu ini")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK
        dataSet.lineWidth = 2f
        dataSet.setCircleColor(Color.BLUE)
        dataSet.circleRadius = 4f

        // Set data to LineChart
        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Customize X-Axis
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return xpData.lineChartData.getOrNull(value.toInt())?.day ?: ""
            }
        }
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true

        // Other chart customizations
        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.invalidate() // Refresh chart
    }
}
