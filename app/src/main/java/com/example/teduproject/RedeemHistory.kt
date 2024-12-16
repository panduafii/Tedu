package com.example.teduproject

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

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
            val historyList = mutableListOf<String>()

            for (child in snapshot.children) {
                val reward = child.child("reward").getValue(String::class.java) ?: "Unknown"
                val cost = child.child("cost").getValue(Int::class.java) ?: 0
                val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L

                historyList.add("Hadiah: $reward | Poin: $cost")
            }

            // Tampilkan data ke ListView
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, historyList)
            listView.adapter = adapter
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mengambil data history.", Toast.LENGTH_SHORT).show()
        }
    }
}
