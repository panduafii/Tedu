package com.example.teduproject

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.teduproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.log10

class Scream : AppCompatActivity() {

    private var mediaRecorder: MediaRecorder? = null
    private lateinit var tvProgress: TextView
    private lateinit var tvCountdown: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnStart: Button
    private lateinit var btnCalibrate: Button

    private var soundLevel = 0
    private var maxSoundLevel = 0
    private var baselineSoundLevel = 0
    private var isRecording = false
    private var isCalibrating = false
    private val REQUEST_AUDIO_PERMISSION = 100

    private var sensitivityFactor = 0.8 // Semakin tinggi nilai, semakin mudah mencapai level 100
    private val maxAllowedAmplitude = 32767 // Batas maksimum amplitude MediaRecorder

    private val soundSamples = mutableListOf<Int>() // Daftar untuk menyimpan rata-rata bergerak suara

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scream)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Kembali ke aktivitas sebelumnya
        }

        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)

        val textTotalPoin = findViewById<TextView>(R.id.textTotalPoin)
        FirebaseHelper.fetchTotalPoin(textTotalPoin, this)

        val streakAtas = findViewById<TextView>(R.id.StreakAtas)
        val streakBawah = findViewById<TextView>(R.id.Streakbawah)
        FirebaseHelper.fetchStreak(streakAtas, streakBawah, this)

        // Inisialisasi views
        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgress)
        tvCountdown = findViewById(R.id.tvCountdown)
        btnStart = findViewById(R.id.btnStart)
        btnCalibrate = findViewById(R.id.btnCalibrate)

        // Periksa izin audio
        if (!hasAudioPermission()) {
            requestPermission()
        }

        // Tombol kalibrasi
        btnCalibrate.setOnClickListener {
            if (hasAudioPermission()) {
                calibrateSoundLevel()
            } else {
                requestPermission()
            }
        }

        // Tombol mulai
        btnStart.setOnClickListener {
            if (hasAudioPermission()) {
                startGame()
            } else {
                requestPermission()
            }
        }
    }

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_AUDIO_PERMISSION
        )
    }

    private fun setupMediaRecorder(): Boolean {
        return try {
            resetMediaRecorder()
            val tempFile = File(cacheDir, "temp_audio.3gp")
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(tempFile.absolutePath)
                prepare()
            }
            Log.d("MainActivity", "MediaRecorder setup completed")
            true
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up MediaRecorder: ${e.message}")
            false
        }
    }

    private fun resetMediaRecorder() {
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error releasing MediaRecorder: ${e.message}")
        } finally {
            mediaRecorder = null
        }
    }

    private fun startRecording(): Boolean {
        return try {
            if (setupMediaRecorder()) {
                Thread.sleep(200)
                mediaRecorder?.start()
                isRecording = true
                Log.d("MainActivity", "Recording started")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error starting recording: ${e.message}")
            false
        }
    }

    private fun calibrateSoundLevel() {
        if (!startRecording()) return

        isCalibrating = true
        baselineSoundLevel = 0
        var totalSoundLevel = 0
        var samples = 0

        val calibrationTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                runOnUiThread {
                    tvCountdown.text = "Kalibrasi: ${millisUntilFinished / 1000} detik tersisa"
                }
            }

            override fun onFinish() {
                Thread {
                    try {
                        for (i in 1..50) {
                            val maxAmplitude = mediaRecorder?.maxAmplitude ?: 0
                            if (maxAmplitude > 0) {
                                totalSoundLevel += maxAmplitude
                                samples++
                            }
                            Thread.sleep(100)
                        }

                        if (samples > 0) {
                            baselineSoundLevel = totalSoundLevel / samples
                        }
                        stopRecording()

                        runOnUiThread {
                            tvProgress.text = "Kalibrasi selesai. Baseline: $baselineSoundLevel"
                        }
                        isCalibrating = false
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error during calibration: ${e.message}")
                        runOnUiThread {
                            tvProgress.text = "Kalibrasi gagal. Silakan coba lagi."
                        }
                        stopRecording()
                        isCalibrating = false
                    }
                }.start()
            }
        }
        calibrationTimer.start()
    }

    private fun startGame() {
        if (!startRecording()) return

        val gameTimer = object : CountDownTimer(10000, 50) {
            override fun onTick(millisUntilFinished: Long) {
                val maxAmplitude = mediaRecorder?.maxAmplitude ?: 0
                if (maxAmplitude > 0) {
                    val normalizedLevel = ((maxAmplitude - baselineSoundLevel).toDouble() /
                            (maxAllowedAmplitude - baselineSoundLevel)) * 100 * sensitivityFactor
                    val newSoundLevel = normalizedLevel.toInt().coerceAtLeast(0)

                    if (soundSamples.size >= 5) {
                        soundSamples.removeAt(0)
                    }
                    soundSamples.add(newSoundLevel)

                    val averageSoundLevel = soundSamples.average().toInt()

                    if (averageSoundLevel > soundLevel) {
                        soundLevel = averageSoundLevel
                    } else {
                        soundLevel = (soundLevel - 2).coerceAtLeast(averageSoundLevel)
                    }

                    maxSoundLevel = maxOf(maxSoundLevel, soundLevel)

                    runOnUiThread {
                        progressBar.progress = soundLevel.coerceAtMost(100)
                        tvProgress.text = "$soundLevel%"
                        tvCountdown.text = "Waktu: ${(millisUntilFinished / 1000.0).format(1)} detik"
                    }
                }
            }

            override fun onFinish() {
                stopRecording()
                runOnUiThread {
                    tvCountdown.text = "Permainan selesai!"
                    tvProgress.text = "Skor akhir: $soundLevel%"
                    saveGameResult(soundLevel) // Simpan poin secara otomatis
                }
            }
        }
        gameTimer.start()
    }

    private fun saveGameResult(finalScore: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Anda belum login.", Toast.LENGTH_SHORT).show()
            return
        }

        val gameData = mapOf(
            "poin" to finalScore.toString(),
            "timestamp" to System.currentTimeMillis().toString()
        )

        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        val gameResultRef = userRef.child("gameResults").push()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Simpan hasil permainan
                gameResultRef.setValue(gameData).await()

                // Ambil `totalPoin` dan tambahkan `finalScore`
                val snapshot = userRef.child("totalPoin").get().await()
                val totalPoin = (snapshot.getValue(Int::class.java) ?: 0) + finalScore

                // Perbarui `totalPoin`
                userRef.child("totalPoin").setValue(totalPoin).await()

                // Tampilkan pesan sukses di main thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Scream, "Hasil permainan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Tampilkan pesan gagal di main thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Scream, "Gagal menyimpan hasil permainan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }




    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("MediaRecorder", "Error stopping MediaRecorder: ${e.message}")
        } finally {
            mediaRecorder = null
            isRecording = false
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_AUDIO_PERMISSION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("MainActivity", "Audio permission granted")
        } else {
            Log.e("MainActivity", "Audio permission denied")
        }
    }
}
