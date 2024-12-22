package com.example.teduproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.teduproject.Scream

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_activity)

        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)

        // Ambil referensi TextView untuk nama pengguna
        val userNameTextView = findViewById<TextView>(R.id.User)

        // Ambil nama pengguna dari Firebase dan atur ke TextView
        FirebaseHelper.fetchUserName { name ->
            userNameTextView.text = name
        }

        val textTotalPoin = findViewById<TextView>(R.id.textTotalPoin)
        FirebaseHelper.fetchTotalPoin(textTotalPoin, this)

        val streakAtas = findViewById<TextView>(R.id.StreakAtas)
        val streakBawah = findViewById<TextView>(R.id.Streakbawah)
        FirebaseHelper.fetchStreak(streakAtas, streakBawah, this)

        // Set OnClickListener untuk ImageView6
        val imageView6: ImageView = findViewById(R.id.imageView6)
        imageView6.setOnClickListener {
            // Pindah ke Activity Scream
            val intent = Intent(this, Scream::class.java)
            startActivity(intent)
        }


    }
}
