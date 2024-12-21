package com.example.teduproject

import android.content.Context
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

object FirebaseHelper {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    // Function to fetch total points directly from Firebase
    fun fetchTotalPoin(targetView: TextView, context: Context) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Anda belum login.", Toast.LENGTH_SHORT).show()
            return
        }

        val totalPoinRef = database.getReference("users").child(userId).child("totalPoin")
        totalPoinRef.get().addOnSuccessListener { snapshot ->
            val totalPoin = snapshot.getValue(Int::class.java) ?: 0
            targetView.text = totalPoin.toString()
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Gagal mengambil total poin: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to fetch and calculate streak based on story timestamps
    fun fetchStreak(targetViewAtas: TextView, targetViewBawah: TextView, context: Context) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid ?: return

        if (userId.isEmpty()) {
            Toast.makeText(context, "Anda belum login.", Toast.LENGTH_SHORT).show()
            return
        }

        val storiesRef = FirebaseDatabase.getInstance().getReference("users/$userId/stories")
        storiesRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val timestamps = snapshot.children.mapNotNull {
                    it.child("timestamp").getValue(String::class.java)?.toLongOrNull() // Handling the string to long conversion here
                }.sorted()

                val streak = calculateStreak(timestamps)
                targetViewAtas.text = streak.toString()
                targetViewBawah.text = streak.toString()
            } else {
                targetViewAtas.text = "0"
                targetViewBawah.text = "0"
                Toast.makeText(context, "Tidak ada data cerita ditemukan.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            targetViewAtas.text = "0"
            targetViewBawah.text = "0"
            Toast.makeText(context, "Gagal mengambil streak: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }



    // General function to calculate streak based on timestamps
    private fun calculateStreak(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0

        val sortedTimestamps = timestamps.sorted()
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date()).toLong()

        var streak = 1  // Start streak count at 1 for the most recent story
        var previousDay = dateFormat.format(Date(timestamps.last())).toLong()

        for (timestamp in sortedTimestamps.reversed().drop(1)) {
            val currentDay = dateFormat.format(Date(timestamp)).toLong()
            if (currentDay == previousDay - 1) {
                streak++
                previousDay = currentDay
            } else if (currentDay < previousDay - 1) {
                break
            }
        }

        return streak
    }

    // Function to calculate total points from the "stories" node
    fun fetchPoinFromStories(targetView: TextView, context: Context) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Anda belum login.", Toast.LENGTH_SHORT).show()
            return
        }

        val storiesRef = database.getReference("users/$userId/stories")
        storiesRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                var totalPoin = 0
                for (storySnapshot in snapshot.children) {
                    val poin = storySnapshot.child("poin").getValue(Int::class.java)
                    totalPoin += poin ?: 0
                }
                targetView.text = totalPoin.toString()
            } else {
                targetView.text = "0"
                Toast.makeText(context, "Data cerita tidak ditemukan.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Gagal mengambil data cerita: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to fetch user name based on the user ID
    fun fetchUserName(callback: (String) -> Unit) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("users").child(userId).child("name")
            userRef.get().addOnSuccessListener { snapshot ->
                val name = snapshot.getValue(String::class.java) ?: "User"
                callback(name)
            }.addOnFailureListener {
                callback("User")  // Fallback if data retrieval fails
            }
        } else {
            callback("User")  // Fallback if user is not logged in
        }
    }
}
