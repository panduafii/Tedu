package com.example.teduproject

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

object BottomNavigationHelper {

    fun setupBottomNavigation(context: Context, navigationView: View) {
        // Menu Reward
        val menuReward = navigationView.findViewById<LinearLayout>(R.id.menu_reward)
        menuReward.setOnClickListener {
            if (context !is Reward) {
                val intent = Intent(context, Reward::class.java)
                context.startActivity(intent)
                // Ganti transisi animasi
                (context as? AppCompatActivity)?.overridePendingTransition(
                    android.R.anim.fade_in, // Animasi masuk
                    android.R.anim.fade_out // Animasi keluar
                )
            }
        }

        // Menu Bercerita
        val menuBercerita = navigationView.findViewById<LinearLayout>(R.id.menu_bercerita)
        menuBercerita.setOnClickListener {
            if (context !is Bercerita) {
                val intent = Intent(context, Bercerita::class.java)
                context.startActivity(intent)
                // Ganti transisi animasi
                (context as? AppCompatActivity)?.overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }
        }

        // Menu Home
        val menuHome = navigationView.findViewById<View>(R.id.menu_home)
        menuHome.setOnClickListener {
            if (context !is MainActivity) {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                // Ganti transisi animasi
                (context as? AppCompatActivity)?.overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }
        }

        // Menu Statistics
        val menuReport = navigationView.findViewById<LinearLayout>(R.id.menu_report)
        menuReport.setOnClickListener {
            if (context !is Report) {
                val intent = Intent(context, Report::class.java)
                context.startActivity(intent)
                // Ganti transisi animasi
                (context as? AppCompatActivity)?.overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }
        }

        // Menu Profile
        val menuProfile = navigationView.findViewById<LinearLayout>(R.id.menu_profile)
        menuProfile.setOnClickListener {
            if (context !is Profile) {
                val intent = Intent(context,  Profile::class.java)
                context.startActivity(intent)
                // Ganti transisi animasi
                (context as? AppCompatActivity)?.overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }
        }
    }
}
