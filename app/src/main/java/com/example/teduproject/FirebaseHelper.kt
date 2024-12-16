package com.example.teduproject

import android.content.Context
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object FirebaseHelper {

    // Fungsi untuk mengambil totalPoin langsung dari Firebase
    fun fetchTotalPoin(targetView: TextView, context: Context) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()

        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Anda belum login.", Toast.LENGTH_SHORT).show()
            return
        }

        // Akses langsung totalPoin dari root pengguna
        val totalPoinRef = database.getReference("users").child(userId).child("totalPoin")
        totalPoinRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val totalPoin = snapshot.getValue(Int::class.java) ?: 0
                targetView.text = totalPoin.toString()
            } else {
                targetView.text = "0"
                Toast.makeText(context, "Total poin belum tersedia.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Gagal mengambil total poin: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk menghitung total poin dari node "stories"
    fun fetchPoinFromStories(targetView: TextView, context: Context) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()

        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Anda belum login.", Toast.LENGTH_SHORT).show()
            return
        }

        // Akses node "stories"
        val storiesRef = database.getReference("users").child(userId).child("stories")
        storiesRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                var totalPoin = 0

                for (storySnapshot in snapshot.children) {
                    val poin = storySnapshot.child("poin").getValue(String::class.java)
                    if (poin != null) {
                        totalPoin += poin.toIntOrNull() ?: 0
                    }
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

    fun fetchUserName(callback: (String) -> Unit) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()

        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("users").child(userId).child("name")
            userRef.get().addOnSuccessListener { snapshot ->
                val name = snapshot.getValue(String::class.java) ?: "User"
                callback(name)
            }.addOnFailureListener {
                callback("User") // Fallback jika gagal mengambil data
            }
        } else {
            callback("User") // Jika user tidak login
        }
    }

}
