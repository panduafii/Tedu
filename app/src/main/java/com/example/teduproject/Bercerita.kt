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
import android.widget.ImageView
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import io.noties.markwon.Markwon
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

        val textTotalPoin = findViewById<TextView>(R.id.textTotalPoin)

        // Panggil fungsi untuk mengambil total poin
        FirebaseHelper.fetchTotalPoin(textTotalPoin, this)

        val headerView = findViewById<View>(R.id.include)
        HeaderNavigationHelper.setupHeaderNavigation(this, headerView)

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
        // ProgressBar untuk menampilkan loading
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

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
                    progressBar.visibility = View.GONE // ProgressBar disembunyikan
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
            progressBar.visibility = View.VISIBLE // Tampilkan ProgressBar
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

            // Ambil nama pengguna dari Firebase
            FirebaseHelper.fetchUserName { userName ->
                val markwon = Markwon.create(this)

                getBalasan(question, userName) { response ->
                    if (response.isNotBlank() && response != "Empty response from server") {
                        hasilBalasan = response
                        runOnUiThread {
                            markwon.setMarkdown(txtBalasan, response) // Gunakan Markwon untuk merender Markdown
                        }
                    }
                }

                getRangkuman(question) { response ->
                    if (response.isNotBlank() && response != "Empty response from server") {
                        hasilRangkuman = response
                        runOnUiThread {
                            markwon.setMarkdown(txtRangkuman, response) // Gunakan Markwon
                        }
                    }
                }

                getKecemasan(question) { response ->
                    if (response.isNotBlank() && response != "Empty response from server") {
                        hasilKecemasan = response
                        runOnUiThread {
                            markwon.setMarkdown(txtKecemasan, response) // Gunakan Markwon
                        }
                    }
                }

                getDepresi(question) { response ->
                    if (response.isNotBlank() && response != "Empty response from server") {
                        hasilDepresi = response
                        runOnUiThread {
                            markwon.setMarkdown(txtDepresi, response) // Gunakan Markwon
                        }
                    }
                }

                getStress(question) { response ->
                    if (response.isNotBlank() && response != "Empty response from server") {
                        hasilStress = response
                        runOnUiThread {
                            markwon.setMarkdown(txtStress, response) // Gunakan Markwon
                        }
                    }
                }

                getPoin(question) { response ->
                    if (response.isNotBlank() && response != "Empty response from server") {
                        hasilPoin = response
                        runOnUiThread {
                            markwon.setMarkdown(txtPoin, response) // Gunakan Markwon
                        }
                    }
                }


                checkResponses() // Mulai pengecekan hingga semua respons diterima
            }

            btnSubmit.visibility = View.GONE
            btnRefresh.visibility = View.VISIBLE
            btnSave.visibility = View.GONE // Sembunyikan save hingga semua data diterima
        }


        btnSave.setOnClickListener {
            progressBar.visibility = View.VISIBLE // Tampilkan ProgressBar saat save
            val cerita = etQuestion.text.toString()

            // Validasi apakah semua hasil sudah terisi
            if (cerita.isBlank()) {
                Toast.makeText(this, "Input cerita tidak boleh kosong.", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE // Sembunyikan jika gagal
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
            progressBar.visibility = View.GONE // Sembunyikan setelah selesai
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

    private fun getLastRangkumanFromFirebase(callback: (String) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val storiesRef = database.getReference("users")
            .child(userId)
            .child("stories")
            .orderByChild("timestamp")
            .limitToLast(1)

        storiesRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    val lastRangkuman = child.child("rangkuman").getValue(String::class.java) ?: ""
                    callback(lastRangkuman)
                }
            } else {
                callback("")
            }
        }.addOnFailureListener {
            callback("")
        }
    }

    private fun getLastBalasanFromFirebase(callback: (String) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val storiesRef = database.getReference("users")
            .child(userId)
            .child("stories")
            .orderByChild("timestamp")
            .limitToLast(1)

        storiesRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    val lastBalasan = child.child("hasilBalasan").getValue(String::class.java) ?: ""
                    callback(lastBalasan)
                }
            } else {
                callback("")
            }
        }.addOnFailureListener {
            callback("")
        }
    }


    private fun getResponse(apiKey: String, url: String, roleContent: String, question: String, callback: (String) -> Unit) {
        val safeRoleContent = JSONObject.quote(roleContent)
        val safeQuestion = JSONObject.quote(question)

        val requestBody = """
        {
            "messages": [
                {
                    "role": "system",
                    "content": $safeRoleContent
                },
                {
                    "role": "user",
                    "content": $safeQuestion
                }
            ],
            "model": "llama3-8b-8192",
            "temperature": 0.7,
            "max_tokens": 1800,
            "top_p": 1
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
                Log.d("DEBUG_RESPONSE", "Raw body: $body")

                if (body.isNullOrEmpty()) {
                    runOnUiThread {
                        callback("Empty response from server")
                    }
                    return
                }

                try {
                    val jsonObject = JSONObject(body)

                    // Cek apakah ada error
                    val errorObj = jsonObject.optJSONObject("error")
                    if (errorObj != null) {
                        val errorMsg = errorObj.optString("message", "Unknown error from server")
                        runOnUiThread {
                            callback("Server Error: $errorMsg")
                        }
                        return
                    }

                    // Baru parsing normal (OpenAI style)
                    val choices = jsonObject.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        val firstChoice = choices.getJSONObject(0)
                        val msgObj = firstChoice.optJSONObject("message")
                        val content = msgObj?.optString("content", "") ?: ""
                        if (content.isNotEmpty()) {
                            runOnUiThread { callback(content) }
                        } else {
                            runOnUiThread { callback("No content found in firstChoice") }
                        }
                    } else {
                        runOnUiThread {
                            callback("No recognized LLM content in response: $body")
                        }
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

    private fun getBalasan(question: String, userName: String, callback: (String) -> Unit) {
        getLastRangkumanFromFirebase { lastRangkuman ->
            val apiKey = "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"
            val url = "https://api.groq.com/openai/v1/chat/completions"

            val roleContent = """
            Halo, $userName! Kamu adalah asisten yang memberikan tanggapan terhadap input pengguna dalam BAHASA INDONESIA mengenai gangguan kecemasan, depresi, dan stres yang mungkin timbul dari cerita pengguna.
            Berikut rangkuman (summary) sebelumnya sebagai konteks tambahan:
            "$lastRangkuman"
                
                Dengan mempertimbangkan rangkuman tersebut, berikan jawaban yang tidak hanya informatif tetapi juga menunjukkan empati dan dukungan. Jawablah dalam BAHASA INDONESIA yang baik dan benar, dan usahakan untuk memberikan saran praktis atau langkah-langkah yang bisa diambil oleh pengguna untuk mengurangi perasaannya saat ini.
            Gunakan nada yang menenangkan dan pastikan untuk validasi perasaan pengguna tanpa mengesampingkan perasaan tersebut. Jika situasinya memerlukan, arahkan pengguna untuk mencari bantuan profesional. jangan lupa untuk selalu menjawab menggunakan bahasa indonesia dan menyisipkan nama pengguna agar terkesan lebih intimate.
            """.trimIndent()

            getResponse(apiKey, url, roleContent, question, callback)
        }
    }

    private fun getRangkuman(question: String, callback: (String) -> Unit) {
        getLastRangkumanFromFirebase { lastRangkuman ->
            val apiKey = "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"
            val url = "https://api.groq.com/openai/v1/chat/completions"

            val roleContent = """
                Kamu adalah asisten yang merangkum input pengguna menjadi poin-poin pentingdalam bahasa Indonesia mengenai gangguan kecemasan, depresi, dan stress yang mungkin timbul dari cerita pengguna.
                Berikut balasan (respon) sebelumnya sebagai konteks tambahan:
                "$lastRangkuman"
                
                Ringkas cerita pengguna baru ini dengan memperhatikan balasan sebelumnya.
            """.trimIndent()

            getResponse(apiKey, url, roleContent, question, callback)
        }
    }

    private fun getKecemasan(question: String, callback: (String) -> Unit) {
        val apiKey = "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"
        val url = "https://api.groq.com/openai/v1/chat/completions"
        val roleContent = "Berdasarkan cerita yang disampaikan, berikan skor kecemasan dari 1 sampai 100. Gunakan panduan berikut untuk penilaian:\n" +
                "        1-10 untuk situasi tanpa kekhawatiran atau stres,\n" +
                "        11-20 untuk sedikit kekhawatiran terhadap situasi spesifik,\n" +
                "        21-30 untuk kekhawatiran yang masih terkelola,\n" +
                "        31-40 untuk kecemasan yang lebih rutin,\n" +
                "        41-50 untuk kecemasan yang mempengaruhi kegiatan sehari-hari,\n" +
                "        51-60 untuk kecemasan intens dan sering,\n" +
                "        61-70 untuk kecemasan yang mengganggu fungsi harian,\n" +
                "        71-80 untuk kecemasan yang melumpuhkan,\n" +
                "        81-90 untuk kekhawatiran yang hampir konstan dan ketakutan signifikan,\n" +
                "        91-100 untuk serangan panik atau keadaan kecemasan mendalam dan berkelanjutan.\n" +
                "        Jawab dengan angka saja, tanpa kata tambahan. contoh, '1','2','3', ... ,'100' "
        getResponse(apiKey, url, roleContent, question, callback)
    }

    private fun getDepresi(question: String, callback: (String) -> Unit) {
        val apiKey = "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"
        val url = "https://api.groq.com/openai/v1/chat/completions"
        val roleContent = "Berdasarkan cerita yang disampaikan, berikan skor depresi dari 1 sampai 100. Gunakan panduan berikut untuk penilaian:\n" +
                "        1-10 untuk suasana hati yang sangat stabil dan positif,\n" +
                "        11-20 untuk sedikit rasa sedih atau down yang jarang terjadi,\n" +
                "        21-30 untuk perasaan down yang terkadang terjadi tanpa pengaruh besar pada kehidupan,\n" +
                "        31-40 untuk suasana hati yang sering down tapi masih bisa berfungsi,\n" +
                "        41-50 untuk perasaan sedih yang rutin dan mulai berdampak pada aktivitas harian,\n" +
                "        51-60 untuk suasana hati yang sering murung dan mengganggu kegiatan sehari-hari,\n" +
                "        61-70 untuk depresi yang signifikan, sering merasa tidak berdaya dan kehilangan minat,\n" +
                "        71-80 untuk depresi yang sangat berat dengan kehilangan minat hampir pada semua aktivitas,\n" +
                "        81-90 untuk perasaan terus-menerus putus asa, kehilangan minat yang parah, dan gangguan fungsi sosial,\n" +
                "        91-100 untuk depresi ekstrem dengan pemikiran sering tentang kematian atau bunuh diri.\n" +
                "        Jawab dengan angka saja, tanpa kata tambahan. contoh, '1','2','3', ... ,'100' "
        getResponse(apiKey, url, roleContent, question, callback)
    }

    private fun getStress(question: String, callback: (String) -> Unit) {
        val apiKey = "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"
        val url = "https://api.groq.com/openai/v1/chat/completions"
        val roleContent = "Berdasarkan cerita yang disampaikan, berikan skor tingkat stres dari 1 sampai 100. Gunakan panduan berikut untuk penilaian:\n" +
                "        1-10 untuk hampir tidak ada stres, merasa sangat tenang dan terkendali,\n" +
                "        11-20 untuk stres sangat ringan, masalah kecil yang mudah diatasi,\n" +
                "        21-30 untuk stres ringan, masalah sehari-hari yang terkadang mengganggu tapi masih terkelola,\n" +
                "        31-40 untuk stres sedang, masalah yang sering mengganggu namun masih bisa diatasi,\n" +
                "        41-50 untuk stres cukup tinggi, masalah yang mulai berdampak pada kesehatan dan kebahagiaan,\n" +
                "        51-60 untuk stres tinggi, sering merasa kewalahan dan kesulitan mengatasi masalah,\n" +
                "        61-70 untuk stres sangat tinggi, merasa kewalahan hampir sepanjang waktu dan sering jatuh sakit,\n" +
                "        71-80 untuk stres ekstrem, kesulitan besar dalam menghadapi hari-hari, sering sakit,\n" +
                "        81-90 untuk stres yang hampir konstan dengan gangguan serius pada kesehatan, pekerjaan, dan hubungan,\n" +
                "        91-100 untuk stres maksimal, hampir tidak mampu berfungsi dalam kehidupan sehari-hari dan mungkin memerlukan bantuan profesional.\n" +
                "        Jawab dengan angka saja, tanpa kata tambahan. contoh, '1','2','3', ... ,'100' "
        getResponse(apiKey, url, roleContent, question, callback)
    }

    private fun getPoin(question: String, callback: (String) -> Unit) {
        val apiKey = "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"
        val url = "https://api.groq.com/openai/v1/chat/completions"
        val roleContent = "Berdasarkan detail dari cerita yang disampaikan pengguna, berikan skor dari 1 sampai 100. Pertimbangkan faktor-faktor berikut dalam penilaian detail:\n" +
                "        1-10 untuk cerita yang sangat umum atau minim detail,\n" +
                "        11-20 untuk cerita dengan sedikit detail spesifik,\n" +
                "        21-30 untuk cerita yang memiliki beberapa detail spesifik,\n" +
                "        31-40 untuk cerita dengan detail yang cukup untuk memahami konteks umum,\n" +
                "        41-50 untuk cerita dengan detail yang baik dan deskripsi beberapa elemen,\n" +
                "        51-60 untuk cerita yang cukup rinci dalam menggambarkan peristiwa atau perasaan,\n" +
                "        61-70 untuk cerita yang detail dalam penggambaran dan emosi,\n" +
                "        71-80 untuk cerita sangat rinci dengan banyak nuansa dan pengamatan kritis,\n" +
                "        81-90 untuk cerita yang sangat detail dan mendalam, memberikan gambaran lengkap dengan banyak insight,\n" +
                "        91-100 untuk cerita yang sangat detail, dengan kekayaan deskripsi dan analisis yang kompleks.\n" +
                "        Jawab hanya dengan angka, tanpa kata tambahan. contoh, '1','2','3', ... ,'100' "
        getResponse(apiKey, url, roleContent, question, callback)
    }
}
