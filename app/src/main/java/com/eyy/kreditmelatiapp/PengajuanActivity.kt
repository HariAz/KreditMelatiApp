package com.eyy.kreditmelatiapp

import Pengajuan
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.pow

class PengajuanActivity : AppCompatActivity() {

    private lateinit var etNama: EditText
    private lateinit var etJumlahPinjaman: EditText
    private lateinit var spinnerTenor: Spinner
    private lateinit var btnAjukan: Button
    private lateinit var dbRef: DatabaseReference
    private lateinit var userRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengajuan)

        etNama = findViewById(R.id.etNama)
        etJumlahPinjaman = findViewById(R.id.etJumlahPinjaman)
        spinnerTenor = findViewById(R.id.spinnerTenor)
        btnAjukan = findViewById(R.id.btnAjukan)

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("pengajuan")
        userRef = FirebaseDatabase.getInstance().getReference("users")

        // Load user data and fill in the name
        loadUserData()

        // Setting up the spinner for tenor selection
        val tenorOptions = arrayOf("1 bulan", "3 bulan", "6 bulan", "12 bulan")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tenorOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTenor.adapter = adapter

        btnAjukan.setOnClickListener {
            savePengajuan()
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        etNama.setText(user.nama)
                    } else {
                        Toast.makeText(this@PengajuanActivity, "Gagal memuat data pengguna", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PengajuanActivity, "Terjadi kesalahan: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun hitungCicilan(jumlahPinjaman: Double, sukuBunga: Double, tenor: Int): Double {
        val bungaBulanan = sukuBunga / 100 / 12
        return (jumlahPinjaman * bungaBulanan * (1 + bungaBulanan).pow(tenor)) /
                ((1 + bungaBulanan).pow(tenor) - 1)
    }

    private fun savePengajuan() {
        val nama = etNama.text.toString().trim()
        val jumlahPinjaman = etJumlahPinjaman.text.toString().trim().toDoubleOrNull()
        val tenorStr = spinnerTenor.selectedItem.toString()

        if (nama.isEmpty() || jumlahPinjaman == null) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (jumlahPinjaman < 500000) {
            Toast.makeText(this, "Jumlah pinjaman minimum adalah Rp500.000", Toast.LENGTH_SHORT).show()
            return
        }

        val tenor = tenorStr.replace(" bulan", "").toInt()

        // Hitung cicilan bulanan dan total hutang
        val sukuBunga = 3.0 // 3% per tahun
        val cicilanBulanan = hitungCicilan(jumlahPinjaman, sukuBunga, tenor)
        val totalHutang = cicilanBulanan * tenor

        val pengajuanId = dbRef.push().key
        val pengajuan = Pengajuan(
            id = pengajuanId,
            userId = auth.currentUser?.uid ?: "",
            nama = nama,
            tanggalPengajuan = System.currentTimeMillis(),  // Menyimpan sebagai timestamp (Long)
            jumlahPinjaman = jumlahPinjaman.toString(),
            tenor = tenorStr,
            sisaHutang = totalHutang, // Menyimpan total hutang
            status = "Berjalan" // Status pengajuan dimulai sebagai "Berjalan"
        )

        pengajuanId?.let {
            dbRef.child(it).setValue(pengajuan).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Pengajuan berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Pengajuan gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
