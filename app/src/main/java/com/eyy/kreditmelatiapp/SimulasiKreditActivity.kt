package com.eyy.kreditmelatiapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow

class SimulasiKreditActivity : AppCompatActivity() {

    private lateinit var etJumlahPinjaman: EditText
    private lateinit var etSukuBunga: EditText
    private lateinit var spinnerTenor: Spinner
    private lateinit var btnHitung: Button
    private lateinit var tvHasilCicilan: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simulasi_kredit)

        etJumlahPinjaman = findViewById(R.id.etJumlahPinjaman)
        etSukuBunga = findViewById(R.id.etSukuBunga)
        spinnerTenor = findViewById(R.id.spinnerTenor)
        btnHitung = findViewById(R.id.btnHitung)
        tvHasilCicilan = findViewById(R.id.tvHasilCicilan)

        val tenorOptions = arrayOf("1 bulan", "3 bulan", "6 bulan", "12 bulan")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tenorOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTenor.adapter = adapter

        // Set suku bunga tetap 3% dan disable EditText agar tidak bisa diubah
        val sukuBungaTetap = 3.0
        etSukuBunga.setText(sukuBungaTetap.toString())
        etSukuBunga.isEnabled = false

        btnHitung.setOnClickListener {
            hitungCicilan(sukuBungaTetap)
        }
    }

    private fun hitungCicilan(sukuBunga: Double) {
        val jumlahPinjaman = etJumlahPinjaman.text.toString().toDoubleOrNull()
        val tenor = spinnerTenor.selectedItem.toString().replace(" bulan", "").toIntOrNull()

        if (jumlahPinjaman == null || tenor == null) {
            Toast.makeText(this, "Harap isi semua data dengan benar", Toast.LENGTH_SHORT).show()
            return
        }

        val bungaBulanan = sukuBunga / 100 / 12
        val cicilan = (jumlahPinjaman * bungaBulanan * (1 + bungaBulanan).pow(tenor)) /
                ((1 + bungaBulanan).pow(tenor) - 1)

        tvHasilCicilan.text = "Cicilan Bulanan: Rp ${"%.2f".format(cicilan)}"
    }
}
