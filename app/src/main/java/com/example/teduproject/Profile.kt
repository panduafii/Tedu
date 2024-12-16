package com.example.teduproject

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Profile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Menghubungkan ID TextView dengan kode Kotlin
        val tvEditProfile: TextView = findViewById(R.id.tvEditProfile)
        val tvLogout: TextView = findViewById(R.id.tvLogout)

        // Aksi ketika Edit Profile ditekan
        tvEditProfile.setOnClickListener {
            Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show()
            // Tambahkan aksi navigasi ke halaman Edit Profile jika diperlukan
        }

        // Aksi ketika Logout ditekan
        tvLogout.setOnClickListener {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            // Tambahkan aksi logout di sini
            finish() // Menutup aktivitas saat ini
        }
    }
}
