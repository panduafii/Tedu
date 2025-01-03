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

    data class DayValue(
        val dateString: String,
        val value: Float
    )

    private var allStories: List<StoryData> = emptyList()

    private lateinit var lineChart: LineChart
    private lateinit var spinnerChartType: Spinner
    private lateinit var tvChartTotal: TextView
    private lateinit var emojiView: ImageView
    private lateinit var anxietyDescription: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)

        // Inisialisasi tombol back
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            // Tutup aktivitas saat tombol back ditekan
            finish()
        }

        val textTotalPoin = findViewById<TextView>(R.id.textTotalPoin)
        FirebaseHelper.fetchTotalPoin(textTotalPoin, this)

        val streakAtas = findViewById<TextView>(R.id.StreakAtas)
        val streakBawah = findViewById<TextView>(R.id.Streakbawah)
        FirebaseHelper.fetchStreak(streakAtas, streakBawah, this)

        lineChart = findViewById(R.id.lineChart)
        spinnerChartType = findViewById(R.id.spinnerChartType)
        tvChartTotal = findViewById(R.id.tvChartTotal)
        anxietyDescription = findViewById(R.id.anxietyDescription)
        emojiView = findViewById(R.id.emojiView)

        val chartTypes = arrayOf("XP per hari", "Depresi per hari", "Stres per hari", "Kecemasan per hari")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, chartTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerChartType.adapter = adapter

        spinnerChartType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> updateChart(computeXpPerDay(allStories), "XP per hari")
                    1 -> updateChart(computeDepresiPerDay(allStories), "Depresi per hari")
                    2 -> updateChart(computeStressPerDay(allStories), "Stres per hari")
                    3 -> updateChart(computeKecemasanPerDay(allStories), "Kecemasan per hari")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Fetch stories and initialize the chart
        fetchStories { storiesList ->
            allStories = storiesList
            if (allStories.isNotEmpty()) {
                updateChart(computeXpPerDay(allStories), "XP per hari") // Menampilkan XP per hari
                val latestStory = allStories.maxByOrNull { it.timestamp }
                latestStory?.let {
                    val (description, emojiResource) = getConditionDescription(it.stress, it.depresi, it.kecemasan)
                    anxietyDescription.text = description
                    emojiView.setImageResource(emojiResource)
                }
                updateDailyStatistics()
            } else {
                anxietyDescription.text = "Data tidak tersedia."
                emojiView.setImageResource(R.drawable.ic_neutral_face)
            }
        }
    }

    private fun updateDailyStatistics() {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val todayStories = allStories.filter {
            val storyDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp))
            storyDate == todayDate
        }

        val totalXpToday = todayStories.sumOf { it.poin.toInt() }

        val averageStress = if (todayStories.isNotEmpty()) todayStories.map { it.stress }.average().toFloat() else 0f
        val averageDepresi = if (todayStories.isNotEmpty()) todayStories.map { it.depresi }.average().toFloat() else 0f
        val averageKecemasan = if (todayStories.isNotEmpty()) todayStories.map { it.kecemasan }.average().toFloat() else 0f

        findViewById<TextView>(R.id.totalXpToday).text = "Total XP: $totalXpToday"
        findViewById<TextView>(R.id.stressToday).text = averageStress.toString()
        findViewById<TextView>(R.id.depresiToday).text = averageDepresi.toString()
        findViewById<TextView>(R.id.kecemasanToday).text = averageKecemasan.toString()
    }

    private fun getConditionDescription(stress: Float, depresi: Float, kecemasan: Float): Pair<String, Int> {
        return when {
            stress in 80f..100f && depresi in 40f..60f && kecemasan in 30f..50f ->
                "Kelelahan 😞" to R.drawable.ic_tired_face
            stress in 70f..90f && depresi in 50f..70f && kecemasan in 20f..40f ->
                "Frustrasi 😠" to R.drawable.ic_frustrated_face
            stress in 50f..80f && depresi in 20f..50f && kecemasan in 60f..90f ->
                "Ketegangan 😰" to R.drawable.ic_tense_face
            stress in 60f..90f && depresi in 30f..50f && kecemasan in 20f..40f ->
                "Irritasi 😡" to R.drawable.ic_irritated_face
            stress in 70f..90f && depresi in 70f..100f && kecemasan in 40f..70f ->
                "Ketidakberdayaan 😔" to R.drawable.ic_helpless_face
            stress in 20f..50f && depresi in 90f..100f && kecemasan in 10f..30f ->
                "Kesedihan Mendalam 😢" to R.drawable.ic_sad_face
            stress in 40f..70f && depresi in 80f..100f && kecemasan in 20f..40f ->
                "Kehilangan Minat 😞" to R.drawable.ic_loss_of_interest
            stress in 30f..60f && depresi in 70f..90f && kecemasan in 30f..50f ->
                "Rasa Bersalah 😔" to R.drawable.ic_guilt
            stress in 50f..80f && depresi in 60f..80f && kecemasan in 50f..80f ->
                "Ketidakpastian 😟" to R.drawable.ic_uncertainty
            stress in 30f..60f && depresi in 30f..60f && kecemasan in 60f..90f ->
                "Gelisah 😰" to R.drawable.ic_restless
            stress in 20f..50f && depresi in 20f..50f && kecemasan in 90f..100f ->
                "Ketakutan Ekstrem 😨" to R.drawable.ic_extreme_fear
            stress in 30f..70f && depresi in 40f..70f && kecemasan in 80f..100f ->
                "Cemas Berlebihan 😟" to R.drawable.ic_excessive_anxiety
            stress in 50f..80f && depresi in 40f..70f && kecemasan in 70f..90f ->
                "Merasa Terancam 😨" to R.drawable.ic_threatened
            stress in 40f..70f && depresi in 90f..100f && kecemasan in 20f..50f ->
                "Kehilangan Harapan 😢" to R.drawable.ic_loss_of_hope
            stress in 20f..50f && depresi in 90f..100f && kecemasan in 10f..30f ->
                "Sensasi Mati Rasa 😶" to R.drawable.ic_numbness
            stress in 0f..20f && depresi in 0f..20f && kecemasan in 0f..20f ->
                "Netral atau Rendah 🙂" to R.drawable.ic_neutral_face
            stress in 0f..50f && depresi in 0f..40f && kecemasan in 30f..50f ->
                "Cemas Ringan 😕" to R.drawable.ic_light_anxiety
            stress in 20f..40f && depresi in 0f..30f && kecemasan in 0f..30f ->
                "Stres Ringan 😌" to R.drawable.ic_light_stress
            else -> "Tidak Diketahui 🤔" to R.drawable.ic_unknown
        }
    }

    private fun fetchStories(callback: (List<StoryData>) -> Unit) {
        FirebaseHelper.fetchStoriesData { stories ->
            callback(stories)
        }
    }

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

    private fun groupStoriesByDate(stories: List<StoryData>): Map<String, List<StoryData>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return stories.groupBy {
            dateFormat.format(Date(it.timestamp))
        }
    }

    private fun updateChart(dayValues: List<DayValue>, label: String) {
        if (dayValues.isEmpty()) {
            lineChart.clear()
            tvChartTotal.text = "Total: 0"
            Toast.makeText(this, "Tidak ada data untuk chart ini", Toast.LENGTH_SHORT).show()
            return
        }

        val entries = dayValues.mapIndexed { index, dv ->
            Entry(index.toFloat(), dv.value)
        }

        val dataSet = LineDataSet(entries, label).apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 2f
            setCircleColor(Color.BLUE)
            circleRadius = 4f
        }
        lineChart.data = LineData(dataSet)

        val xAxis = lineChart.xAxis
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val idx = value.toInt()
                return dayValues.getOrNull(idx)?.dateString ?: ""
            }
        }

        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false

        lineChart.invalidate()

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
