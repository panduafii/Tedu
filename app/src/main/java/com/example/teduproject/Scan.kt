package com.example.teduproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.noties.markwon.Markwon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit

class Scan : AppCompatActivity() {
    private lateinit var scanButton: Button
    private lateinit var imageDisplay: ImageView
    private lateinit var textResultDisplay: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var photoFile: File
    private lateinit var spinnerUsers: Spinner

    private val userNames = mutableListOf<String>()
    private val userDetailsMap = mutableMapOf<String, UserData>()

    private var selectedUserData: UserData? = null

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_CAMERA_PERMISSION = 101
        const val API_URL = "https://api.openai.com/v1/chat/completions"
        const val API_KEY = "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" // Ganti dengan API key Anda
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        // Bottom Navigation setup
        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)

        val backButton = findViewById<ImageView>(R.id.logo)
        backButton.setOnClickListener {
            finish() // Kembali ke aktivitas sebelumnya
        }

        // Fetch and display streak data
        val streakAtas = findViewById<TextView>(R.id.StreakAtas)
        val streakBawah = findViewById<TextView>(R.id.Streakbawah)
        FirebaseHelper.fetchStreak(streakAtas, streakBawah, this)

        // Fetch and display total points
        val textTotalPoin = findViewById<TextView>(R.id.textTotalPoin)
        FirebaseHelper.fetchTotalPoin(textTotalPoin, this)

        scanButton = findViewById(R.id.scanButton)
        imageDisplay = findViewById(R.id.imageDisplay)
        textResultDisplay = findViewById(R.id.textResultDisplay)
        progressBar = findViewById(R.id.progressBar)
        spinnerUsers = findViewById(R.id.spinnerUsers)

        fetchUserData()

        spinnerUsers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedName = userNames[position]
                selectedUserData = userDetailsMap[selectedName]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedUserData = null
            }
        }

        scanButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            } else {
                dispatchTakePictureIntent()
            }
        }
        // Periksa apakah intent memiliki flag untuk langsung membuka kamera
        val openCamera = intent.getBooleanExtra("OPEN_CAMERA", false)
        if (openCamera) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            } else {
                dispatchTakePictureIntent()
            }
        }


    }

    private fun fetchUserData() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Log.d("Firebase", "User tidak terautentikasi")
            Toast.makeText(this, "User tidak terautentikasi", Toast.LENGTH_SHORT).show()
            return
        }

        val kesehatanRef = FirebaseDatabase.getInstance().getReference("users/$currentUserId/kesehatan")
        Log.d("Firebase", "Mengambil data kesehatan dari: users/$currentUserId/kesehatan")

        kesehatanRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userNames.clear()
                userDetailsMap.clear()
                Log.d("Firebase", "Jumlah data ditemukan: ${snapshot.childrenCount}")

                for (dataSnapshot in snapshot.children) {
                    val userData = dataSnapshot.getValue(UserData::class.java)
                    if (userData != null) {
                        Log.d("Firebase", "Data pengguna ditemukan: ${userData.nama}")
                        userNames.add(userData.nama)
                        userDetailsMap[userData.nama] = userData
                    }
                }

                updateSpinner()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Gagal mengambil data: ${error.message}")
                Toast.makeText(this@Scan, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun updateSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUsers.adapter = adapter
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                photoFile = File.createTempFile("photo", ".jpg", cacheDir)
                val photoURI = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            imageBitmap?.let {
                imageDisplay.setImageBitmap(it)

                // Tampilkan ProgressBar
                progressBar.visibility = View.VISIBLE

                // Lakukan analisis gambar
                performImageAnalysis(it) {
                    progressBar.visibility = View.GONE // Sembunyikan ProgressBar setelah selesai
                }
            }
        }
    }

    private fun performImageAnalysis(image: Bitmap, onComplete: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val selectedUserData = this@Scan.selectedUserData ?: run {
                    Log.e("ImageAnalysis", "Pengguna belum dipilih")
                    runOnUiThread {
                        Toast.makeText(this@Scan, "Pilih pengguna terlebih dahulu", Toast.LENGTH_SHORT).show()
                    }
                    onComplete()
                    return@launch
                }

                val encodedImage = encodeImageToBase64(image)
                Log.d("ImageAnalysis", "Encoded image size: ${encodedImage.length}")

                val prompt = """
                Berikut adalah data kesehatan saya, Nama: ${selectedUserData.nama}, Umur: ${selectedUserData.umur}, Berat Badan: ${selectedUserData.berat_badan} kg, Tinggi Badan: ${selectedUserData.tinggi_badan} cm, Alergi: ${selectedUserData.alergi.ifEmpty { "Tidak ada" }}, Pantangan: ${selectedUserData.pantangan.ifEmpty { "Tidak ada" }}, Kondisi Umum: ${selectedUserData.kondisi_umum}. Analisis gambar makanan diberikan.
            """.trimIndent()
                Log.d("ImageAnalysis", "Prompt created: $prompt")

                val response = sendImageToOpenAi(encodedImage, prompt)
                Log.d("ImageAnalysis", "API Response: $response")

                runOnUiThread {
                    val markwon = Markwon.create(this@Scan)
                    markwon.setMarkdown(textResultDisplay, response)
                    processOpenAIResponse(response)
                    onComplete()
                }
            } catch (e: Exception) {
                Log.e("ImageAnalysis", "Error: ${e.localizedMessage}")
                runOnUiThread {
                    textResultDisplay.text = "Error: ${e.localizedMessage}"
                    onComplete()
                }
            }
        }
    }

    private fun encodeImageToBase64(image: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 45, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun sendImageToOpenAi(encodedImage: String, prompt: String): String {
        Log.d("OpenAI", "Mengirim gambar ke OpenAI dengan panjang base64: ${encodedImage.length}")
        Log.d("OpenAI", "Prompt: $prompt")

        val jsonPayload = """
        {
          "model": "gpt-4o-mini",
          "messages": [
            {
              "role": "system",
              "content": [
                {
                  "type": "text",
                  "text": "Kamu adalah seorang analis gizi. Diberikan gambar makanan kemasan dengan daftar informasi nutrisi yang tercetak pada kemasannya. Tolong analisis dan bandingkan dengan data kesehatan yang telah pengguna berikan, pastikan bahwa analisis yang kamu berikan sesuai dengan data pada Nutrition Facts atau Gizi pada gambar dengan data kesehatan pengguna. Pastikan kamu juga memberikan inferensi dan keluaran dari open ai bahwa makanan tersebut BISA DIKONSUMSI/HATI-HATI DIKONSUMSI/TIDAK BISA DIKONSUMSI, buatlah salah satu keluarab dari ketiga hal itu supaya di capslock. lalu pastikan juga kamu menulis dalam baha indonesia. tulisan dengan rapi dengan struktur yaitu Rekomendasi,informasi, Analisis, barulah kesimpulan. pastikan strukturnya tepat. jangan lupa sebutkan nama pengguna agar lebih intimate. jangan lupa bold inferensi agar mudah terbaca"
                }
              ]
            },
            {
              "role": "user",
              "content": [
                {
                  "type": "image_url",
                  "image_url": {
                    "url": "data:image/jpeg;base64,$encodedImage"
                  }
                },
                {
                  "type": "text",
                  "text": "ini adalah gambar nutriton facts atau gizi dari makanan yang akan saya beli. Ini adalah data kesehatan saya $prompt"
                }
              ]
            }
          ],
          "response_format": {
            "type": "text"
          },
          "temperature": 0.7,
          "max_completion_tokens": 2548,
          "top_p": 0.9,
          "frequency_penalty": 0,
          "presence_penalty": 0.5
        }    
    """.trimIndent()

        Log.d("OpenAI", "Payload: $jsonPayload")

        val requestBody = jsonPayload.toRequestBody("application/json".toMediaType())
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(API_URL)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $API_KEY")
            .build()

        return client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
            if (!response.isSuccessful || responseBody == null) {
                Log.e("OpenAI", "Gagal mengirim ke API: ${response.message}")
                return "Error: ${response.message}"
            }

            val jsonResponse = JSONObject(responseBody)
            Log.d("OpenAI", "Response JSON: $jsonResponse")
            val choicesArray = jsonResponse.getJSONArray("choices")
            val firstChoice = choicesArray.getJSONObject(0)
            val messageObject = firstChoice.getJSONObject("message")
            return messageObject.getString("content")
        }
    }

    private fun processOpenAIResponse(response: String) {
        val category = when {
            response.contains("BISA DIKONSUMSI", ignoreCase = true) -> 1
            response.contains("TIDAK BISA DIKONSUMSI", ignoreCase = true) -> 2
            response.contains("HATI-HATI DIKONSUMSI", ignoreCase = true) -> 3
            else -> 0 // Jika tidak ada kategori yang cocok
        }

        runOnUiThread {
            findViewById<TextView>(R.id.textBoleh).visibility = if (category == 1) View.VISIBLE else View.GONE
            findViewById<TextView>(R.id.textTidakBoleh).visibility = if (category == 2) View.VISIBLE else View.GONE
            findViewById<TextView>(R.id.textHatiHati).visibility = if (category == 3) View.VISIBLE else View.GONE

            Log.d("OpenAI", "Kategori yang dihasilkan: $category")
        }

        if (category != 0) {
            saveResultToFirebase(category)
        }
    }

    private fun saveResultToFirebase(category: Int) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("users/$currentUserId/lastResult")
        dbRef.setValue(category).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Firebase", "Hasil kategori berhasil disimpan: $category")
            } else {
                Log.e("Firebase", "Gagal menyimpan hasil kategori: ${task.exception?.message}")
            }
        }
    }



}
