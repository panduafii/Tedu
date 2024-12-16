package com.example.teduproject

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_activity)

        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)

        val textTotalPoin = findViewById<TextView>(R.id.textTotalPoin)
        FirebaseHelper.fetchTotalPoin(textTotalPoin, this)

    }
}
