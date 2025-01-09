package com.example.teduproject

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
                intent.putExtra("USE_DEFAULT_USER", true) // Tambahkan flag untuk membuka kamera langsung

                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId != null) {
                    val userRef = FirebaseDatabase.getInstance().getReference("users/$currentUserId/kesehatan")
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                Toast.makeText(context, "Data kesehatan tidak ditemukan di database", Toast.LENGTH_SHORT).show()
                                Log.e("FirebaseDebug", "Data kesehatan tidak ditemukan di path 'users/$currentUserId/kesehatan'")
                                return
                            }

                            for (dataSnapshot in snapshot.children) {
                                val userData = dataSnapshot.getValue(UserData::class.java)
                                Log.d("FirebaseDebug", "Data kesehatan ditemukan: ${userData?.nama}")

                                if (userData != null && userData.nama.equals("Pandu", ignoreCase = true)) {
                                    // Pastikan pengguna default ditemukan
                                    intent.putExtra("SELECTED_USER", userData.nama)
                                    break
                                }
                            }

                            // Mulai activity dengan flag yang sudah diatur
                            context.startActivity(intent)
                            (context as? AppCompatActivity)?.overridePendingTransition(
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                            )
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                }
            }
        }


        // Menu Statistics diganti jadi isi data pengguna
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
                val intent = Intent(context, Profile::class.java)
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
