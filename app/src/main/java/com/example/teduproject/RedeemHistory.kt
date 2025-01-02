package com.example.teduproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class RedeemHistory : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_redeem_history)

        firebaseAuth = FirebaseAuth.getInstance()
        val listView = findViewById<ListView>(R.id.historyListView)

        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)

        // Ambil referensi TextView dari header_bar
        val textTotalPoin = findViewById<TextView>(R.id.textTotalPoin)

        // Panggil fungsi untuk mengambil total poin
        FirebaseHelper.fetchTotalPoin(textTotalPoin, this)

        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Anda belum login.", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil data history redeem dari Firebase
        val redeemedRef = database.getReference("users").child(userId).child("redeemedRewards")
        redeemedRef.get().addOnSuccessListener { snapshot ->
            val historyList = mutableListOf<RedeemHistoryItem>()
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

            for (child in snapshot.children) {
                val reward = child.child("reward").getValue(String::class.java) ?: "Unknown"
                val cost = child.child("cost").getValue(Int::class.java) ?: 0
                val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                val formattedDate = dateFormat.format(Date(timestamp))

                historyList.add(RedeemHistoryItem(reward, cost, formattedDate))
            }
            // Tambahkan logika untuk tombol Back
            val backButton = findViewById<ImageView>(R.id.backButton)
            backButton.setOnClickListener {
                // Navigasi kembali ke halaman Bercerita
                val intent = Intent(this, Reward::class.java)
                startActivity(intent)
                finish() // Tutup aktivitas saat ini
            }

            // Tampilkan data ke ListView dengan adapter
            val adapter = RedeemHistoryAdapter(this, historyList)
            listView.adapter = adapter
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mengambil data history.", Toast.LENGTH_SHORT).show()
        }
    }
}
