package com.example.teduproject

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class Report : AppCompatActivity() {

    // Data class mewakili hasil pengelompokan per hari
    data class DayValue(
        val dateString: String,
        val value: Float
    )

    // Menyimpan
    // semua data story yang diambil dari Firebase

    private var allStories: List<StoryData> = emptyList()

    // View
    private lateinit var lineChart: LineChart
    private lateinit var spinnerChartType: Spinner
    private lateinit var tvChartTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // Setup Bottom Navigation
        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)

        // Ambil referensi TextView dari header_bar (untuk total poin)
        val textTotalPoin = findViewById<TextView>(R.id.textTotalPoin)
        FirebaseHelper.fetchTotalPoin(textTotalPoin, this)

        // Streak (jika diperlukan)
        val streakAtas = findViewById<TextView>(R.id.StreakAtas)
        val streakBawah = findViewById<TextView>(R.id.Streakbawah)
        FirebaseHelper.fetchStreak(streakAtas, streakBawah, this)

        // Inisialisasi View Chart, Spinner, dan TV total
        lineChart = findViewById(R.id.lineChart)
        spinnerChartType = findViewById(R.id.spinnerChartType)
        tvChartTotal = findViewById(R.id.tvChartTotal)

        // Siapkan pilihan chart di Spinner
        val chartTypes = arrayOf("XP per hari", "Depresi per hari", "Stres per hari", "Kecemasan per hari")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, chartTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerChartType.adapter = adapter

        // Listener ketika user mengubah pilihan chart
        spinnerChartType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Panggil fungsi perhitungan & update chart sesuai pilihan
                when (position) {
                    0 -> updateChart(computeXpPerDay(allStories), "XP per hari")
                    1 -> updateChart(computeDepresiPerDay(allStories), "Depresi per hari")
                    2 -> updateChart(computeStressPerDay(allStories), "Stres per hari")
                    3 -> updateChart(computeKecemasanPerDay(allStories), "Kecemasan per hari")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Ambil data stories dari Firebase, lalu tampilkan chart default
        fetchStories { storiesList ->
            allStories = storiesList
            if (allStories.isEmpty()) {
                Toast.makeText(this, "Tidak ada data cerita di Firebase.", Toast.LENGTH_SHORT).show()
                lineChart.clear()
            } else {
                // Set spinner ke 0 (XP per hari) sebagai default
                spinnerChartType.setSelection(0)
            }
        }
    }

    // ----------------------------------
    // Ambil data stories dari Firebase
    // ----------------------------------
    private fun fetchStories(callback: (List<StoryData>) -> Unit) {
        FirebaseHelper.fetchStoriesData { stories ->
            callback(stories)
        }
    }

    // ----------------------------------
    // Fungsi hitung "poin", "depresi", dsb. per tanggal
    // ----------------------------------

    private fun computeXpPerDay(stories: List<StoryData>): List<DayValue> {
        val grouped = groupStoriesByDate(stories)
        return grouped.map { (dateStr, listStories) ->
            val totalXp = listStories.map { it.poin }.sum()
            DayValue(dateStr, totalXp)
        }.sortedBy { it.dateString }
    }

    private fun computeDepresiPerDay(stories: List<StoryData>): List<DayValue> {
        val grouped = groupStoriesByDate(stories)
        return grouped.map { (dateStr, listStories) ->
            val totalDepresi = listStories.map { it.depresi }.sum()
            DayValue(dateStr, totalDepresi)
        }.sortedBy { it.dateString }
    }

    private fun computeStressPerDay(stories: List<StoryData>): List<DayValue> {
        val grouped = groupStoriesByDate(stories)
        return grouped.map { (dateStr, listStories) ->
            val totalStress = listStories.map { it.stress }.sum()
            DayValue(dateStr, totalStress)
        }.sortedBy { it.dateString }
    }

    private fun computeKecemasanPerDay(stories: List<StoryData>): List<DayValue> {
        val grouped = groupStoriesByDate(stories)
        return grouped.map { (dateStr, listStories) ->
            val totalKecemasan = listStories.map { it.kecemasan }.sum()
            DayValue(dateStr, totalKecemasan)
        }.sortedBy { it.dateString }
    }

    /**
     * Mengelompokkan StoryData berdasarkan tanggal (format "yyyy-MM-dd").
     */
    private fun groupStoriesByDate(stories: List<StoryData>): Map<String, List<StoryData>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return stories.groupBy {
            dateFormat.format(Date(it.timestamp))
        }
    }

    // ----------------------------------
    // Update Chart & Tampilkan Total
    // ----------------------------------
    private fun updateChart(dayValues: List<DayValue>, label: String) {
        if (dayValues.isEmpty()) {
            lineChart.clear()
            tvChartTotal.text = "Total: 0"
            Toast.makeText(this, "Tidak ada data untuk chart ini", Toast.LENGTH_SHORT).show()
            return
        }

        // Buat Entry untuk MPAndroidChart
        val entries = dayValues.mapIndexed { index, dv ->
            Entry(index.toFloat(), dv.value)
        }

        // Buat DataSet
        val dataSet = LineDataSet(entries, label).apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 2f
            setCircleColor(Color.BLUE)
            circleRadius = 4f
        }
        lineChart.data = LineData(dataSet)

        // X-Axis
        val xAxis = lineChart.xAxis
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val idx = value.toInt()
                return dayValues.getOrNull(idx)?.dateString ?: ""
            }
        }

        // Matikan axisRight, hilangkan deskripsi, dll.
        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false

        // Refresh chart
        lineChart.invalidate()

        // -----------------------------
        // Hitung total & tampilkan
        // -----------------------------
        val sumValue = dayValues.map { it.value }.sum()  // total
        val prefix = when (label) {
            "XP per hari" -> "Total XP"
            "Depresi per hari" -> "Total Depresi"
            "Stres per hari" -> "Total Stres"
            "Kecemasan per hari" -> "Total Kecemasan"
            else -> "Total"
        }
        tvChartTotal.text = "$prefix: ${sumValue.toInt()}"
    }
}
