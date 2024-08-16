package com.eyy.kreditmelatiapp

import Pengajuan
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userRef: DatabaseReference
    private lateinit var dbRef: DatabaseReference
    private lateinit var tvWelcome: TextView
    private lateinit var btnPengajuan: Button
    private lateinit var btnSimulasi: Button
    private lateinit var btnCekKredit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Inisialisasi Firebase Authentication dan Realtime Database
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("pengajuan")
        userRef = FirebaseDatabase.getInstance().getReference("users")

        // Inisialisasi view dari layout
        tvWelcome = findViewById(R.id.tvWelcome)
        btnPengajuan = findViewById(R.id.btnPengajuan)
        btnSimulasi = findViewById(R.id.btnSimulasi)
        btnCekKredit = findViewById(R.id.btnKredit)

        // Load data pengguna dan tampilkan nama di dashboard
        loadUserData()

        // Tombol untuk memulai pengajuan kredit baru
        btnPengajuan.setOnClickListener {
            cekStatusKredit()
        }

        // Tombol untuk membuka simulasi kredit
        btnSimulasi.setOnClickListener{
            startActivity(Intent(this, SimulasiKreditActivity::class.java))
        }

        // Tombol untuk membuka halaman cek status kredit
        btnCekKredit.setOnClickListener{
            startActivity(Intent(this, CekKreditActivity::class.java))
        }
    }

    // Fungsi untuk memuat data pengguna dari database
    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        tvWelcome.text = "Selamat Datang, ${user.nama}"
                    } else {
                        Toast.makeText(this@DashboardActivity, "Gagal memuat data pengguna", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DashboardActivity, "Terjadi kesalahan: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // Fungsi untuk mengecek apakah pengguna masih memiliki kredit yang berjalan
    private fun cekStatusKredit() {
        val userId = auth.currentUser?.uid ?: return

        dbRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var masihAdaKreditBerjalan = false

                    for (data in snapshot.children) {
                        val pengajuan = data.getValue(Pengajuan::class.java)
                        if (pengajuan?.status == "berjalan") {
                            masihAdaKreditBerjalan = true
                            break
                        }
                    }

                    if (masihAdaKreditBerjalan) {
                        Toast.makeText(this@DashboardActivity, "Anda masih memiliki kredit yang belum lunas.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Jika tidak ada kredit yang belum lunas, pengguna bisa mengajukan kredit baru
                        startActivity(Intent(this@DashboardActivity, PengajuanActivity::class.java))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DashboardActivity, "Terjadi kesalahan: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
