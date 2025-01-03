package com.example.teduproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_CAMERA_PERMISSION = 101
        const val API_URL = "https://api.openai.com/v1/chat/completions"
        const val API_KEY = "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" // Ganti dengan API key Anda
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        scanButton = findViewById(R.id.scanButton)
        imageDisplay = findViewById(R.id.imageDisplay)
        textResultDisplay = findViewById(R.id.textResultDisplay)
        progressBar = findViewById(R.id.progressBar)

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
                val encodedImage = encodeImageToBase64(image)

                // Kirim ke OpenAI
                val response = sendImageToOpenAi(encodedImage)
                runOnUiThread {
                    textResultDisplay.text = response
                    onComplete()
                }
            } catch (e: Exception) {
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

    private fun sendImageToOpenAi(encodedImage: String): String {
        val jsonPayload = """
        {
          "model": "gpt-4o-mini",
          "messages": [
            {
              "role": "system",
              "content": [
                {
                  "type": "text",
                  "text": "Kamu adalah seorang analis gizi. Diberikan gambar makanan kemasan dengan daftar informasi nutrisi yang tercetak pada kemasannya. Tolong analisis dan tuliskan kembali informasi nutrisi tersebut secara terperinci dan detail, pastikan bahwa nilai yang kamu tulis sesuai dengan yang ada di gambar. lalu pastikan juga kamu menulis dalam baha indonesia. tulisan dengan rapi"
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
                  "text": "ini adalah gambarnya"
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
                return "Error: ${response.message}"
            }

            val jsonResponse = JSONObject(responseBody)
            val choicesArray = jsonResponse.getJSONArray("choices")
            val firstChoice = choicesArray.getJSONObject(0)
            val messageObject = firstChoice.getJSONObject("message")
            return messageObject.getString("content")
        }
    }
}
