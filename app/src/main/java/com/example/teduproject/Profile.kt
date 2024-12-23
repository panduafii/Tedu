package com.example.teduproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Profile : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase Auth and Database Reference
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Setup navigation and other UI elements (assuming setupBottomNavigation is a valid method)
        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)

        // Ambil referensi TextView dari header_bar
        val textTotalPoin = findViewById<TextView>(R.id.textTotalPoin)

        // Panggil fungsi untuk mengambil total poin
        FirebaseHelper.fetchTotalPoin(textTotalPoin, this)

        val streakAtas = findViewById<TextView>(R.id.StreakAtas)
        val streakBawah = findViewById<TextView>(R.id.Streakbawah)
        FirebaseHelper.fetchStreak(streakAtas, streakBawah, this)

        // Ambil referensi TextView untuk nama pengguna
        val userNameTextView = findViewById<TextView>(R.id.tvUsername)

        // Ambil nama pengguna dari Firebase dan atur ke TextView
        FirebaseHelper.fetchUserName { name ->
            userNameTextView.text = name
        }

        val tvLogout: TextView = findViewById(R.id.tvLogout)
        val tvEditProfile: TextView = findViewById(R.id.tvEditProfile)
        val tvHapusBercerita: TextView = findViewById(R.id.tvHapusBercerita)

        // Event handler for logout
        tvLogout.setOnClickListener {
            // Perform Firebase sign out
            firebaseAuth.signOut()

            // Show a message to the user
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

            // Redirect to the SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish() // Finish the current activity to remove it from the back stack
        }

        // Setup for other UI elements like edit profile
        tvEditProfile.setOnClickListener {
            // Handle edit profile click
            Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show()
            // Navigate to Edit Profile Activity if necessary
        }

        // Setup for delete stories
        tvHapusBercerita.setOnClickListener {
            showDeleteConfirmationDialog()
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
        // UID pengguna saat ini
        val currentUserId = firebaseAuth.currentUser?.uid

        // Cek jika UID ada
        if (currentUserId != null) {
            // Referensi ke lokasi stories pengguna
            val userStoriesRef = databaseReference.child("users/$currentUserId/stories")

            // Hapus semua stories
            userStoriesRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Semua stories telah dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal menghapus stories", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Pengguna tidak dikenali, tidak bisa menghapus stories", Toast.LENGTH_SHORT).show()
        }
    }
}
