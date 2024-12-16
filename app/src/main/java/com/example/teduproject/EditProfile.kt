package com.example.teduproject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditProfile : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnEditPassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Hubungkan UI ke variabel
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnEditPassword = findViewById(R.id.btnEditPassword)

        // Aksi untuk tombol Edit Password
        btnEditPassword.setOnClickListener {
            // Aktifkan EditText Password
            etPassword.isEnabled = true
            etPassword.requestFocus()

            // Tampilkan pesan
            Toast.makeText(this, "You can now edit the password", Toast.LENGTH_SHORT).show()
        }

        // Contoh aksi untuk menyimpan perubahan (opsional)
        // Bisa ditambahkan tombol simpan di layout jika diperlukan
    }
}
