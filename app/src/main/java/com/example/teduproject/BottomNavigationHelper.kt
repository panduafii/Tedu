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

        // Menu Scan
        val menuScan = navigationView.findViewById<View>(R.id.menu_scan)
        menuScan.setOnClickListener {
            if (context !is Scan) {
                val intent = Intent(context, Scan::class.java)
                intent.putExtra("OPEN_CAMERA", true) // Tambahkan flag untuk membuka kamera langsung
                context.startActivity(intent)
                // Ganti transisi animasi
                (context as? AppCompatActivity)?.overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }
        }


        // Menu Statistics siganti jadi isi data pengguna
        val menuKesehatan = navigationView.findViewById<LinearLayout>(R.id.menu_datapengguna)
        menuKesehatan.setOnClickListener {
            if (context !is Kesehatan) {
                val intent = Intent(context, Kesehatan::class.java)
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
