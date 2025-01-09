package com.example.teduproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Reward : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private lateinit var rewardsRecyclerView: RecyclerView
    private lateinit var adapter: RewardsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reward)


        // Ambil referensi TextView dari header_bar
        val textTotalPoin = findViewById<TextView>(R.id.textTotalPoin)

        // Panggil fungsi untuk mengambil total poin
        FirebaseHelper.fetchTotalPoin(textTotalPoin, this)

        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)

        val streakAtas = findViewById<TextView>(R.id.StreakAtas)
        val streakBawah = findViewById<TextView>(R.id.Streakbawah)
        FirebaseHelper.fetchStreak(streakAtas, streakBawah, this)

        firebaseAuth = FirebaseAuth.getInstance()

        // Set up RecyclerView
        rewardsRecyclerView = findViewById(R.id.rewardsRecyclerView)
        rewardsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Dummy rewards data
        val rewardsList = listOf(
            RewardItem("Voucher Konsultasi Psikolog RS JIH", 200, R.drawable.jih.toString()),
            RewardItem("Voucher Discount XXI", 100, R.drawable.cinemaxxi.toString()),
            RewardItem("Voucher Hiburan Premium Joox", 50, R.drawable.joox.toString()),
            RewardItem("Voucher Discount Indomaret", 10, R.drawable.indomaret.toString()),
        )


        adapter = RewardsAdapter(rewardsList) { reward ->
            showRedeemDialog(reward)
        }
        rewardsRecyclerView.adapter = adapter

        // Set up history button
        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, RedeemHistory::class.java))
        }
    }

    private fun showRedeemDialog(reward: RewardItem) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Redeem")
        builder.setMessage("Apakah Anda yakin ingin menukar ${reward.points} poin untuk ${reward.name}?")
        builder.setPositiveButton("Ya") { _, _ ->
            redeemPoints(reward)
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun redeemPoints(reward: RewardItem) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "Gagal mengidentifikasi pengguna. Silakan login ulang.", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = database.getReference("users").child(userId)

        userRef.child("totalPoin").get().addOnSuccessListener { snapshot ->
            val totalPoin = snapshot.getValue(Int::class.java) ?: 0
            if (totalPoin >= reward.points) {
                // Update total points
                userRef.child("totalPoin").setValue(totalPoin - reward.points).addOnSuccessListener {
                    val redeemedRef = userRef.child("redeemedRewards").push()
                    val redeemedData = mapOf(
                        "reward" to reward.name,
                        "cost" to reward.points,
                        "timestamp" to System.currentTimeMillis()
                    )
                    redeemedRef.setValue(redeemedData)
                    Toast.makeText(this, "Berhasil menukar ${reward.points} poin!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Poin Anda tidak mencukupi.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mengambil total poin.", Toast.LENGTH_SHORT).show()
        }
    }
}