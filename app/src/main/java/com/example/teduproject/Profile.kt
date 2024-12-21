package com.example.teduproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Profile : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Setup navigation and other UI elements (assuming setupBottomNavigation is a valid method)
        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)

        // Ambil referensi TextView dari header_bar
        val textTotalPoin = findViewById<TextView>(R.id.textTotalPoin)

        // Panggil fungsi untuk mengambil total poin
        FirebaseHelper.fetchTotalPoin(textTotalPoin, this)

        val streakAtas = findViewById<TextView>(R.id.StreakAtas)
        val streakBawah = findViewById<TextView>(R.id.Streakbawah)
        FirebaseHelper.fetchStreak(streakAtas, streakBawah, this)

        // Ambil referensi TextView untuk nama pengguna
        val userNameTextView = findViewById<TextView>(R.id.tvUsername)

        // Ambil nama pengguna dari Firebase dan atur ke TextView
        FirebaseHelper.fetchUserName { name ->
            userNameTextView.text = name
        }

        val tvLogout: TextView = findViewById(R.id.tvLogout)

        // Event handler for logout
        tvLogout.setOnClickListener {
            // Perform Firebase sign out
            firebaseAuth.signOut()

            // Show a message to the user
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

            // Redirect to the SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish() // Finish the current activity to remove it from the back stack
        }

        // Setup for other UI elements like edit profile
        val tvEditProfile: TextView = findViewById(R.id.tvEditProfile)
        tvEditProfile.setOnClickListener {
            // Handle edit profile click
            Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show()
            // Navigate to Edit Profile Activity if necessary
        }
    }
}
