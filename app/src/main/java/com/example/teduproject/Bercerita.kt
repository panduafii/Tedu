package com.example.teduproject

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class Bercerita : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bercerita)

        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)
    }
}
