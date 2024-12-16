package com.example.teduproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Reward : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reward)

        firebaseAuth = FirebaseAuth.getInstance()

        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)

        val textTotalPoin = findViewById<TextView>(R.id.textTotalPoin)
        FirebaseHelper.fetchTotalPoin(textTotalPoin, this)

        val btnRedeem1 = findViewById<Button>(R.id.btnRedeem1)
        btnRedeem1.setOnClickListener {
            showRedeemDialog(100, "Voucher XXI")
        }

        val btnHistory = findViewById<Button>(R.id.btnHistory)
        btnHistory.setOnClickListener {
            startActivity(Intent(this, RedeemHistory::class.java))
        }
    }

    private fun showRedeemDialog(cost: Int, rewardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Redeem")
        builder.setMessage("Apakah Anda yakin ingin menukar $cost poin untuk $rewardName?")
        builder.setPositiveButton("Ya") { _, _ ->
            redeemPoints(cost, rewardName)
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun redeemPoints(cost: Int, rewardName: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val userRef = database.getReference("users").child(userId)

        userRef.child("totalPoin").get().addOnSuccessListener { snapshot ->
            var totalPoin = snapshot.getValue(Int::class.java) ?: 0

            if (totalPoin >= cost) {
                totalPoin -= cost
                userRef.child("totalPoin").setValue(totalPoin).addOnSuccessListener {
                    val redeemedRef = userRef.child("redeemedRewards").push()
                    val redeemedData = mapOf(
                        "reward" to rewardName,
                        "cost" to cost,
                        "timestamp" to System.currentTimeMillis()
                    )
                    redeemedRef.setValue(redeemedData)
                    Toast.makeText(this, "Berhasil menukar $cost poin!", Toast.LENGTH_SHORT).show()
                    FirebaseHelper.fetchTotalPoin(findViewById(R.id.textTotalPoin), this)
                }
            } else {
                Toast.makeText(this, "Poin Anda tidak mencukupi.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mengambil total poin.", Toast.LENGTH_SHORT).show()
        }
    }
}
