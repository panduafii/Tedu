package com.example.teduproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.teduproject.Bercerita
import com.example.teduproject.Reward
import com.example.teduproject.Report
import com.example.teduproject.Scream

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_activity)

        // Bottom Navigation setup
        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)

        // Fetch and display streak data
        val streakAtas = findViewById<TextView>(R.id.StreakAtas)
        val streakBawah = findViewById<TextView>(R.id.Streakbawah)
        FirebaseHelper.fetchStreak(streakAtas, streakBawah, this)

        // Fetch and display total points
        val textTotalPoin = findViewById<TextView>(R.id.textTotalPoin)
        FirebaseHelper.fetchTotalPoin(textTotalPoin, this)

        // Set user greeting
        val titleGreeting = findViewById<TextView>(R.id.titleGreeting)
        val titleUser = findViewById<TextView>(R.id.titleUser)

        FirebaseHelper.fetchUserName { name ->
            titleGreeting.text = "Good To See You"
            titleUser.text = name
        }


        // Set onClickListeners for the cards
        val cardUngu = findViewById<ImageView>(R.id.i_ungu)
        val cardKuning = findViewById<ImageView>(R.id.i_kuning)
        val cardHijau = findViewById<ImageView>(R.id.i_hijau)
        val cardPink = findViewById<ImageView>(R.id.i_pink)

        cardUngu.setOnClickListener {
            val intent = Intent(this, Bercerita::class.java)
            startActivity(intent)
        }

        cardKuning.setOnClickListener {
            val intent = Intent(this, Scream::class.java)
            startActivity(intent)
        }

        cardHijau.setOnClickListener {
            val intent = Intent(this, Reward::class.java)
            startActivity(intent)
        }

        cardPink.setOnClickListener {
            val intent = Intent(this, Report::class.java)
            startActivity(intent)
        }
    }
}
