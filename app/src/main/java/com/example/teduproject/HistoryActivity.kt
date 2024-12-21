
package com.example.teduproject
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private var historyList: MutableList<HistoryItem> = mutableListOf()
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Ambil referensi TextView dari header_bar
        val textTotalPoin = findViewById<TextView>(R.id.textTotalPoin)

        // Panggil fungsi untuk mengambil total poin
        FirebaseHelper.fetchTotalPoin(textTotalPoin, this)

        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)

        recyclerView = findViewById(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(historyList)
        recyclerView.adapter = adapter

        // Initialize Firebase Auth and Database Reference
        val firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid ?: return // Get current user id, return if null

        databaseReference = FirebaseDatabase.getInstance().getReference("users/$userId/stories")
        fetchHistoryData()
    }

    private fun fetchHistoryData() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                historyList.clear()
                for (postSnapshot in snapshot.children) {
                    val history = postSnapshot.getValue(HistoryItem::class.java)
                    history?.let { historyList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors
            }
        })
    }

}

data class HistoryItem(
    val cerita: String? = "",
    val depresi: String? = "",
    val kecemasan: String? = "",
    val poin: String? = "",
    val rangkuman: String? = "",
    val stress: String? = "",
    val timestamp: String? = ""
)
