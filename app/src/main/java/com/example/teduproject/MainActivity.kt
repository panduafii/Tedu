package com.example.teduproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main_page)

        val navigationView = findViewById<View>(R.id.navigationCard)
        val menuReward = navigationView.findViewById<LinearLayout>(R.id.menu_reward)
        menuReward.setOnClickListener {
            val intent = Intent(this, Reward ::class.java)
            startActivity(intent)
        }

        val menuLibrary = navigationView.findViewById<LinearLayout>(R.id.menu_library)
        menuLibrary.setOnClickListener {
            val intent = Intent(this, Challenge ::class.java)
            startActivity(intent)
        }
        
        val menuStatistics = navigationView.findViewById<LinearLayout>(R.id.menu_statistics)
        menuStatistics.setOnClickListener {
            val intent = Intent(this, Report ::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}