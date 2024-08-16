package com.eyy.kreditmelatiapp

import Pengajuan
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

class CekKreditActivity : AppCompatActivity() {

    private lateinit var tvNama: TextView
    private lateinit var tvAlamat: TextView
    private lateinit var tvNomorHP: TextView
    private lateinit var tvStatusKredit: TextView
    private lateinit var tvHistoriPembayaran: TextView
    private lateinit var tvTotalBayar: TextView
    private lateinit var btnKonfirmasiPembayaran: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var dbUser: DatabaseReference

    private var cicilanBulanan = 0.0  // Cicilan per bulan
    private var totalHutang = 0.0  // Hutang yang tersisa
    private var tenor: Int = 0  // Tenor dalam bulan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cek_kredit)

        // Inisialisasi View
        tvNama = findViewById(R.id.tvNamaa)
        tvAlamat = findViewById(R.id.tvAlamatt)
        tvNomorHP = findViewById(R.id.tvNomorHPP)
        tvStatusKredit = findViewById(R.id.tvStatusKreditt)
        tvHistoriPembayaran = findViewById(R.id.tvHistoriPembayarann)
        tvTotalBayar = findViewById(R.id.tvTotalBayar)
        btnKonfirmasiPembayaran = findViewById(R.id.btnKonfirmasiPembayaran)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("pengajuan")
        dbUser = FirebaseDatabase.getInstance().getReference("users")

        // Load data pengguna dan pengajuan
        loadUserData()

        // Action untuk konfirmasi pembayaran
        btnKonfirmasiPembayaran.setOnClickListener {
            konfirmasiPembayaran()
        }
    }

    fun calculateDueDate(tanggalPinjam: Long): Date {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = tanggalPinjam
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -3)
        return calendar.time
    }

    fun formatTanggal(tanggal: Date): String {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        return dateFormat.format(tanggal)
    }

    fun hitungCicilan(jumlahPinjaman: Double, sukuBunga: Double, tenor: Int): Double {
        val bungaBulanan = sukuBunga / 100 / 12
        return (jumlahPinjaman * bungaBulanan * (1 + bungaBulanan).pow(tenor)) /
                ((1 + bungaBulanan).pow(tenor) - 1)
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            dbRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            var pengajuanAktifDitemukan = false

                            for (data in snapshot.children) {
                                val pengajuan = data.getValue(Pengajuan::class.java)
                                if (pengajuan != null) {
                                    if (pengajuan.status == "Berjalan") {
                                        pengajuanAktifDitemukan = true

                                        tvStatusKredit.text = "Berjalan"
                                        val tenggatWaktu = calculateDueDate(pengajuan.tanggalPengajuan)
                                        tvHistoriPembayaran.text = "Harus dibayar sebelum: ${formatTanggal(tenggatWaktu)}"

                                        cicilanBulanan = hitungCicilan(
                                            pengajuan.jumlahPinjaman.toDouble(),
                                            3.0,  // Suku bunga tetap 3%
                                            pengajuan.tenor.replace(" bulan", "").toInt()
                                        )

                                        totalHutang = pengajuan.sisaHutang
                                        tenor = pengajuan.tenor.replace(" bulan", "").toInt()

                                        // Tampilkan cicilan bulanan sebagai total yang harus dibayar untuk periode ini
                                        tvTotalBayar.text = "Total yang harus dibayar: Rp ${"%.2f".format(cicilanBulanan)}"

                                        // Jika tidak ada sisa hutang, nonaktifkan tombol konfirmasi pembayaran
                                        if (totalHutang <= 0) {
                                            btnKonfirmasiPembayaran.isEnabled = false
                                            tvStatusKredit.text = "Tidak ada pengajuan aktif"
                                            tvHistoriPembayaran.text = "-"
                                            tvTotalBayar.text = "-"
                                        }

                                        break
                                    }
                                }
                            }

                            // Jika tidak ada pengajuan dengan status "Berjalan"
                            if (!pengajuanAktifDitemukan) {
                                tvStatusKredit.text = "Tidak ada pengajuan aktif"
                                tvHistoriPembayaran.text = "-"
                                tvTotalBayar.text = "-"
                                btnKonfirmasiPembayaran.isEnabled = false
                            }
                        } else {
                            tvStatusKredit.text = "Tidak ada pengajuan aktif"
                            tvHistoriPembayaran.text = "-"
                            tvTotalBayar.text = "-"
                            btnKonfirmasiPembayaran.isEnabled = false
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@CekKreditActivity, "Terjadi kesalahan: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })

            dbUser.child(userId).get().addOnSuccessListener { dataSnapshot ->
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    tvNama.text = user.nama
                    tvAlamat.text = user.alamat
                    tvNomorHP.text = user.nomorHP
                } else {
                    Toast.makeText(this, "Gagal memuat data pengguna", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Terjadi kesalahan: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun konfirmasiPembayaran() {
        val userId = auth.currentUser?.uid ?: return

        dbRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            val pengajuanRef = data.ref

                            // Kurangi total hutang dengan cicilan bulanan
                            totalHutang -= cicilanBulanan

                            if (totalHutang <= 0) {
                                // Jika sisa hutang sudah lunas, perbarui status ke "Selesai" sebelum menyimpan ke database
                                pengajuanRef.child("sisaHutang").setValue(0) // Mengatur sisa hutang ke 0
                                pengajuanRef.child("status").setValue("Selesai")
                                pengajuanRef.child("statusPembayaran").setValue("Terverifikasi")
                                Toast.makeText(this@CekKreditActivity, "Pembayaran lunas dan kredit selesai.", Toast.LENGTH_SHORT).show()

                                // Update tampilan jika pengajuan selesai
                                btnKonfirmasiPembayaran.isEnabled = false
                                tvStatusKredit.text = "Tidak ada pengajuan aktif"
                                tvHistoriPembayaran.text = "-"
                                tvTotalBayar.text = "-"
                            } else {
                                pengajuanRef.child("sisaHutang").setValue(totalHutang)
                                Toast.makeText(this@CekKreditActivity, "Pembayaran periode ini berhasil, sisa hutang: Rp ${"%.2f".format(totalHutang)}", Toast.LENGTH_SHORT).show()
                            }

                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CekKreditActivity, "Terjadi kesalahan: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
