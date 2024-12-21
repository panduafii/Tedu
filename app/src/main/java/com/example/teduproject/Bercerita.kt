package com.example.teduproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class Bercerita : AppCompatActivity() {
    private val client = OkHttpClient()
    private lateinit var firebaseAuth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bercerita)

        // Ambil referensi TextView dari header_bar
        val textTotalPoin = findViewById<TextView>(R.id.textTotalPoin)

        // Panggil fungsi untuk mengambil total poin
        FirebaseHelper.fetchTotalPoin(textTotalPoin, this)

        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)

        val streakAtas = findViewById<TextView>(R.id.StreakAtas)
        val streakBawah = findViewById<TextView>(R.id.Streakbawah)
        FirebaseHelper.fetchStreak(streakAtas, streakBawah, this)

        val txtSpeechBubble = findViewById<TextView>(R.id.speechBubble)

        // Ambil nama pengguna dari Firebase dan perbarui TextView
        FirebaseHelper.fetchUserName { name ->
            val greeting = "Halo, $name! Kamu ingin bercerita apa hari ini?"
            runOnUiThread {
                txtSpeechBubble.text = greeting
            }
        }
        // Initialize the button and set an onClickListener
        val btnHistory = findViewById<Button>(R.id.btnHistory)
        btnHistory.setOnClickListener {
            // Start HistoryActivity
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }



        firebaseAuth = FirebaseAuth.getInstance()

        val etQuestion = findViewById<EditText>(R.id.inputField)
        val btnSubmit = findViewById<Button>(R.id.submitButton)
        val btnSave = findViewById<Button>(R.id.tombolSimpan)
        val btnRefresh = findViewById<Button>(R.id.tombolRefresh)
        val txtBalasan = findViewById<TextView>(R.id.txtBalasan)
        val txtRangkuman = findViewById<TextView>(R.id.txtRangkuman)
        val txtKecemasan = findViewById<TextView>(R.id.txtKecemasan)
        val txtDepresi = findViewById<TextView>(R.id.txtDepresi)
        val txtStress = findViewById<TextView>(R.id.txtStress)
        val txtPoin = findViewById<TextView>(R.id.txtPoin)

        var hasilBalasan = ""
        var hasilRangkuman = ""
        var hasilKecemasan = ""
        var hasilDepresi = ""
        var hasilStress = ""
        var hasilPoin = ""

        btnSubmit.visibility = View.VISIBLE
        btnSave.visibility = View.GONE
        btnRefresh.visibility = View.GONE

        val handler = Handler(Looper.getMainLooper())

        fun checkResponses() {
            handler.postDelayed({
                val allResponsesReady = listOf(
                    hasilBalasan,
                    hasilRangkuman,
                    hasilKecemasan,
                    hasilDepresi,
                    hasilStress,
                    hasilPoin
                ).all { it.isNotBlank() }

                if (allResponsesReady) {
                    btnSubmit.visibility = View.GONE
                    btnSave.visibility = View.VISIBLE
                    btnRefresh.visibility = View.VISIBLE

                    // Menampilkan resultContainer setelah semua data diterima
                    val resultContainer = findViewById<LinearLayout>(R.id.resultContainer)
                    resultContainer.visibility = View.VISIBLE

                    Toast.makeText(this, "Semua data berhasil diperoleh!", Toast.LENGTH_SHORT).show()
                } else {
                    checkResponses()  // Jika belum lengkap, ulangi pengecekan
                }
            }, 500)
        }


        btnSubmit.setOnClickListener {
            val question = etQuestion.text.toString()

            if (question.isBlank()) {
                Toast.makeText(this, "Input tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Mengirim pertanyaan, harap tunggu...", Toast.LENGTH_SHORT).show()

            hasilBalasan = ""
            hasilRangkuman = ""
            hasilKecemasan = ""
            hasilDepresi = ""
            hasilStress = ""
            hasilPoin = ""

            txtBalasan.text = "Loading..."
            txtRangkuman.text = "Loading..."
            txtKecemasan.text = "Loading..."
            txtDepresi.text = "Loading..."
            txtStress.text = "Loading..."
            txtPoin.text = "Loading..."

            // Panggil semua API
            getBalasan(question) { response ->
                if (response.isNotBlank() && response != "Empty response from server") {
                    hasilBalasan = response
                    runOnUiThread { txtBalasan.text = response }
                }
            }
            getRangkuman(question) { response ->
                if (response.isNotBlank() && response != "Empty response from server") {
                    hasilRangkuman = response
                    runOnUiThread { txtRangkuman.text = response }
                }
            }
            getKecemasan(question) { response ->
                if (response.isNotBlank() && response != "Empty response from server") {
                    hasilKecemasan = response
                    runOnUiThread { txtKecemasan.text = response }
                }
            }
            getDepresi(question) { response ->
                if (response.isNotBlank() && response != "Empty response from server") {
                    hasilDepresi = response
                    runOnUiThread { txtDepresi.text = response }
                }
            }
            getStress(question) { response ->
                if (response.isNotBlank() && response != "Empty response from server") {
                    hasilStress = response
                    runOnUiThread { txtStress.text = response }
                }
            }
            getPoin(question) { response ->
                if (response.isNotBlank() && response != "Empty response from server") {
                    hasilPoin = response
                    runOnUiThread { txtPoin.text = response }
                }
            }

            checkResponses() // Mulai pengecekan hingga semua respons diterima

            btnSubmit.visibility = View.GONE
            btnRefresh.visibility = View.VISIBLE
            btnSave.visibility = View.GONE // Sembunyikan save hingga semua data diterima
        }

        btnSave.setOnClickListener {
            val cerita = etQuestion.text.toString()

            // Validasi apakah semua hasil sudah terisi
            if (cerita.isBlank()) {
                Toast.makeText(this, "Input cerita tidak boleh kosong.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (hasilBalasan.isBlank() || hasilRangkuman.isBlank() || hasilKecemasan.isBlank() ||
                hasilDepresi.isBlank() || hasilStress.isBlank() || hasilPoin.isBlank()) {
                Toast.makeText(this, "Harap tunggu hingga semua hasil diproses.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Jika validasi lolos, simpan data ke Firebase
            saveToFirebase(
                cerita,
                hasilBalasan,
                hasilRangkuman,
                hasilKecemasan,
                hasilDepresi,
                hasilStress,
                hasilPoin
            )
        }

        btnRefresh.setOnClickListener {
            btnSubmit.performClick() // Panggil ulang semua proses submit
        }
    }

    private fun saveToFirebase(
        cerita: String,
        hasilBalasan: String,
        hasilRangkuman: String,
        hasilKecemasan: String,
        hasilDepresi: String,
        hasilStress: String,
        hasilPoin: String
    ) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Anda belum login.", Toast.LENGTH_SHORT).show()
            return
        }

        val ceritaData = mapOf(
            "cerita" to cerita,
            "hasilBalasan" to hasilBalasan,
            "rangkuman" to hasilRangkuman,
            "kecemasan" to hasilKecemasan,
            "depresi" to hasilDepresi,
            "stress" to hasilStress,
            "poin" to hasilPoin,
            "timestamp" to System.currentTimeMillis().toString()
        )

        val userRef = database.getReference("users").child(userId)
        val ceritaRef = userRef.child("stories").push()

        // Simpan cerita
        ceritaRef.setValue(ceritaData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Tambahkan poin ke totalPoin
                userRef.child("totalPoin").get().addOnSuccessListener { snapshot ->
                    var totalPoin = snapshot.getValue(Int::class.java) ?: 0
                    totalPoin += hasilPoin.toIntOrNull() ?: 0

                    userRef.child("totalPoin").setValue(totalPoin).addOnCompleteListener { totalTask ->
                        if (totalTask.isSuccessful) {
                            Toast.makeText(this, "Cerita berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Gagal memperbarui total poin.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Gagal menyimpan cerita: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getResponse(apiKey: String, url: String, roleContent: String, question: String, callback: (String) -> Unit) {
        val requestBody = """
        {
            "messages": [
                {
                    "role": "system",
                    "content": "$roleContent"
                },
                {
                    "role": "user",
                    "content": "$question"
                }
            ],
            "model": "llama3-8b-8192",
            "temperature": 0.7,
            "max_tokens": 1800,
            "top_p": 1,
            "stream": true
        }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API Error", "API request failed", e)
                runOnUiThread {
                    callback("API request failed: ${e.localizedMessage}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body.isNullOrEmpty()) {
                    runOnUiThread {
                        callback("Empty response from server")
                    }
                    return
                }

                try {
                    val lines = body.split("\n")
                    val fullContent = StringBuilder()

                    for (line in lines) {
                        if (line.startsWith("data: ")) {
                            val jsonPart = line.removePrefix("data: ").trim()
                            if (jsonPart == "[DONE]") continue

                            val jsonObject = JSONObject(jsonPart)
                            val choices = jsonObject.optJSONArray("choices") ?: continue
                            for (i in 0 until choices.length()) {
                                val delta = choices.getJSONObject(i).getJSONObject("delta")
                                val content = delta.optString("content", "")
                                fullContent.append(content)
                            }
                        }
                    }

                    runOnUiThread {
                        callback(fullContent.toString())
                    }
                } catch (e: Exception) {
                    Log.e("JSON Parse Error", "Error parsing JSON", e)
                    runOnUiThread {
                        callback("Error parsing server response: ${e.message}")
                    }
                }
            }
        })
    }

    private fun getBalasan(question: String, callback: (String) -> Unit) {
        val apiKey = "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"
        val url = "https://api.groq.com/openai/v1/chat/completions"
        val roleContent = "Kamu adalah asisten yang memberikan tanggapan terhadap input pengguna."
        getResponse(apiKey, url, roleContent, question, callback)
    }

    private fun getRangkuman(question: String, callback: (String) -> Unit) {
        val apiKey = "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"
        val url = "https://api.groq.com/openai/v1/chat/completions"
        val roleContent = "Kamu adalah asisten yang merangkum input pengguna menjadi poin-poin penting."
        getResponse(apiKey, url, roleContent, question, callback)
    }

    private fun getKecemasan(question: String, callback: (String) -> Unit) {
        val apiKey = "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"
        val url = "https://api.groq.com/openai/v1/chat/completions"
        val roleContent = "saya ingin kamu menilai tingkat kecemasan dari cerita user, saya ingin kamu menjawab dalam '1-100', tidak ada kata kata lain, hanya angka saja, contoh, '1','2','3', ... ,'100' "
        getResponse(apiKey, url, roleContent, question, callback)
    }

    private fun getDepresi(question: String, callback: (String) -> Unit) {
        val apiKey = "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"
        val url = "https://api.groq.com/openai/v1/chat/completions"
        val roleContent = "saya ingin kamu menilai tingkat depresi dari cerita user, saya ingin kamu menjawab dalam '1-100', tidak ada kata kata lain, hanya angka saja, contoh, '1','2','3', ... ,'100' "
        getResponse(apiKey, url, roleContent, question, callback)
    }

    private fun getStress(question: String, callback: (String) -> Unit) {
        val apiKey = "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"
        val url = "https://api.groq.com/openai/v1/chat/completions"
        val roleContent = "saya ingin kamu menilai tingkat stress dari cerita user, saya ingin kamu menjawab dalam '1-100', tidak ada kata kata lain, hanya angka saja, contoh, '1','2','3', ... ,'100' "
        getResponse(apiKey, url, roleContent, question, callback)
    }

    private fun getPoin(question: String, callback: (String) -> Unit) {
        val apiKey = "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"
        val url = "https://api.groq.com/openai/v1/chat/completions"
        val roleContent = "Hitung poin atau skor berdasarkan masukan pengguna dan konteks yang diberikan.saya ingin kamu menjawab dalam '1-100', tidak ada kata kata lain, hanya angka saja, contoh, '1','2','3', ... ,'100' "
        getResponse(apiKey, url, roleContent, question, callback)
    }
}
