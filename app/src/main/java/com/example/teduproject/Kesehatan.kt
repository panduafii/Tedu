package com.example.teduproject

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Kesehatan : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: KesehatanAdapter
    private val userList = mutableListOf<UserData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kesehatan)

        val mainView = findViewById<View>(R.id.kesehatan)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        databaseReference = FirebaseDatabase.getInstance().getReference("users/${currentUser.uid}")

        val navigationView = findViewById<View>(R.id.navigationCard)
        BottomNavigationHelper.setupBottomNavigation(this, navigationView)
        val textTotalPoin = findViewById<TextView>(R.id.textTotalPoin)
        FirebaseHelper.fetchTotalPoin(textTotalPoin, this)
        val streakAtas = findViewById<TextView>(R.id.StreakAtas)
        val streakBawah = findViewById<TextView>(R.id.Streakbawah)
        FirebaseHelper.fetchStreak(streakAtas, streakBawah, this)

        // Dropdown functionality for "Data Pribadi"
        val arrowDataPribadi = findViewById<ImageView>(R.id.arrowDataPribadi)
        val detailDataPribadi = findViewById<LinearLayout>(R.id.detailDataPribadi)
        val buttonIsi = findViewById<Button>(R.id.buttonIsi)
        var isDataPribadiExpanded = false

        recyclerView = findViewById(R.id.recyclerViewKesehatan)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = KesehatanAdapter(userList)
        recyclerView.adapter = adapter

        fetchDataFromFirebase()

        arrowDataPribadi.setOnClickListener {
            isDataPribadiExpanded = !isDataPribadiExpanded
            detailDataPribadi.visibility = if (isDataPribadiExpanded) View.VISIBLE else View.GONE
            buttonIsi.visibility = if (isDataPribadiExpanded) View.VISIBLE else View.GONE
            arrowDataPribadi.setImageResource(
                if (isDataPribadiExpanded) R.drawable.circle_chevron_down else R.drawable.circle_chevron_down
            )
        }

        // Dropdown functionality for "Data Orang Lain"
        val arrowDataOrangLain = findViewById<ImageView>(R.id.arrowDataOrangLain)
        val detailDataOrangLain = findViewById<LinearLayout>(R.id.detailDataOrangLain)
        val buttonTambah = findViewById<Button>(R.id.buttonTambah)
        var isDataOrangLainExpanded = false

        arrowDataOrangLain.setOnClickListener {
            isDataOrangLainExpanded = !isDataOrangLainExpanded
            detailDataOrangLain.visibility = if (isDataOrangLainExpanded) View.VISIBLE else View.GONE
            buttonTambah.visibility = if (isDataOrangLainExpanded) View.VISIBLE else View.GONE
            arrowDataOrangLain.setImageResource(
                if (isDataOrangLainExpanded) R.drawable.circle_chevron_down else R.drawable.circle_chevron_down
            )
        }

        buttonIsi.setOnClickListener{
            showPopupIsi()
        }

        buttonTambah.setOnClickListener {
            showPopupTambah()
        }
    }

    private fun fetchDataFromFirebase() {
        // Ubah path ke "users/{currentUser.uid}/kesehatan" agar sesuai dengan lokasi penyimpanan data
        val kesehatanRef = FirebaseDatabase.getInstance().getReference("users/${firebaseAuth.currentUser?.uid}/kesehatan")
        kesehatanRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (dataSnapshot in snapshot.children) {
                    val userData = dataSnapshot.getValue(UserData::class.java)
                    if (userData != null) {
                        userList.add(userData)
                    }
                }
                adapter.notifyDataSetChanged()  // Refresh RecyclerView
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Kesehatan, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showPopupTambah() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.activity_popup_tambah, null)

        val inputNama = view.findViewById<EditText>(R.id.inputNama)
        val inputUmur = view.findViewById<EditText>(R.id.inputUmur)
        val radioPria = view.findViewById<RadioButton>(R.id.rbPria)
        val inputAlergi = view.findViewById<EditText>(R.id.inputAlergiMakanan)
        val cbDiabetes = view.findViewById<CheckBox>(R.id.cbDiabetes)
        val cbHipertensi = view.findViewById<CheckBox>(R.id.cbHipertensi)
        val cbPenyakitJantung = view.findViewById<CheckBox>(R.id.cbPenyakitJantung)
        val inputPantangan = view.findViewById<EditText>(R.id.inputPantanganMakanan)
        val inputBeratBadan = view.findViewById<EditText>(R.id.inputBeratBadan)
        val inputTinggiBadan = view.findViewById<EditText>(R.id.inputTinggiBadan)
        val inputKondisiUmum = view.findViewById<EditText>(R.id.inputKondisiKesehatan)
        val inputMakananKonsumsi = view.findViewById<EditText>(R.id.inputMakananKonsumsi)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = inputNama.text.toString()
                val umur = inputUmur.text.toString().toIntOrNull() ?: 0
                val jenisKelamin = if (radioPria.isChecked) "Pria" else "Wanita"
                val alergi = inputAlergi.text.toString()

                val kondisiKesehatan = mutableListOf<String>()
                if (cbDiabetes.isChecked) kondisiKesehatan.add("Diabetes")
                if (cbHipertensi.isChecked) kondisiKesehatan.add("Hipertensi")
                if (cbPenyakitJantung.isChecked) kondisiKesehatan.add("Penyakit Jantung")

                val pantangan = inputPantangan.text.toString()
                val beratBadan = inputBeratBadan.text.toString().toFloatOrNull() ?: 0f
                val tinggiBadan = inputTinggiBadan.text.toString().toFloatOrNull() ?: 0f
                val kondisiUmum = inputKondisiUmum.text.toString()
                val makananKonsumsi = inputMakananKonsumsi.text.toString()

                saveDataToFirebase(
                    UserData(
                        nama,
                        umur,
                        jenis_kelamin = jenisKelamin,
                        alergi,
                        kondisi_kesehatan = kondisiKesehatan,
                        pantangan,
                        berat_badan = beratBadan,
                        tinggi_badan = tinggiBadan,
                        kondisi_umum = kondisiUmum,
                        makanan_konsumsi = makananKonsumsi
                    )
                )
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }
    private fun showPopupIsi() {
        // Inflate the custom layout
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.activity_popup_isi, null)

        // Create AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("Simpan") { dialog, _ ->
                // Handle save action here
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun saveDataToFirebase(userData: UserData) {
        val currentUserId = FirebaseHelper.getCurrentUserId()
        if (currentUserId != null) {
            val kesehatanRef = FirebaseHelper.getDatabaseReference("users/$currentUserId/kesehatan").push()
            kesehatanRef.setValue(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data kesehatan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menyimpan data kesehatan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Pengguna tidak terautentikasi.", Toast.LENGTH_SHORT).show()
        }
    }
}