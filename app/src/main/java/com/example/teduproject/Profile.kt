package com.example.teduproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Profile : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize Database Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users/${currentUser.uid}")

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Menutup aktivitas dan kembali ke layar sebelumnya
        }

        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)

        val textTotalPoin = findViewById<TextView>(R.id.textTotalPoin)
        FirebaseHelper.fetchTotalPoin(textTotalPoin, this)

        val streakAtas = findViewById<TextView>(R.id.StreakAtas)
        val streakBawah = findViewById<TextView>(R.id.Streakbawah)
        FirebaseHelper.fetchStreak(streakAtas, streakBawah, this)

        val userNameTextView = findViewById<TextView>(R.id.tvUsername)
        FirebaseHelper.fetchUserName { name ->
            userNameTextView.text = name
        }

        val tvLogout: TextView = findViewById(R.id.tvLogout)
        val tvEditProfile: TextView = findViewById(R.id.tvEditProfile)
        val tvHapusBercerita: TextView = findViewById(R.id.tvHapusBercerita)
        val informasiKesehatan: TextView = findViewById(R.id.InformasiKesehatan)

        informasiKesehatan.setOnClickListener {
            val intent = Intent(this, Kesehatan::class.java)
            startActivity(intent)
        }

        tvLogout.setOnClickListener {
            firebaseAuth.signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        tvEditProfile.setOnClickListener {
            Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show()
        }

        tvHapusBercerita.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // Fetch and display the number of stories
        val tvMentalHealth: TextView = findViewById(R.id.tvMentalHealth)
        databaseReference.child("stories").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvMentalHealth.text = snapshot.childrenCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error fetching story data", Toast.LENGTH_SHORT).show()
            }
        })

        // Fetch and display the number of games played
        val tvStoriesWritten: TextView = findViewById(R.id.tvStoriesWritten)
        databaseReference.child("gameResults").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvStoriesWritten.text = snapshot.childrenCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error fetching game results", Toast.LENGTH_SHORT).show()
            }
        })

        val progressBarPoints = findViewById<ProgressBar>(R.id.progressBarPoints)
        progressBarPoints.max = 365  // Set the maximum value of the progress bar to 365

        // Fetch and display the count of stories
        val storyCountRef = databaseReference.child("stories")
        storyCountRef.get().addOnSuccessListener { dataSnapshot ->
            val count = dataSnapshot.childrenCount.toInt() // Get the count of stories
            progressBarPoints.progress = count // Update the progress bar
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Failed to fetch story count: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Hapus")
        builder.setMessage("Apakah Anda yakin ingin menghapus semua stories Anda?")
        builder.setPositiveButton("Ya") { _, _ ->
            hapusStory()
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun hapusStory() {
        databaseReference.child("stories").removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Semua stories telah dihapus", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal menghapus stories", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
