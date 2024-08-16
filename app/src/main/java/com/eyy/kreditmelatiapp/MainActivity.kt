package com.eyy.kreditmelatiapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        FirebaseApp.initializeApp(this)

        // Inisialisasi Firebase
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("pengajuan")

        val btnSimulasi = findViewById<MaterialButton>(R.id.btnSimulasi)
        val btnPengajuan = findViewById<MaterialButton>(R.id.btnPengajuan)
        val btnFAQ = findViewById<MaterialButton>(R.id.btnFAQ)

        btnSimulasi.setOnClickListener {
            startActivity(Intent(this, SimulasiKreditActivity::class.java))
        }

        btnPengajuan.setOnClickListener {
            startActivity(Intent(this, PengajuanActivity::class.java))
        }

        btnFAQ.setOnClickListener {
            startActivity(Intent(this, FAQActivity::class.java))
        }
    }
}