data class Pengajuan(
    val id: String? = "",
    val userId: String = "",
    val nama: String = "",
    val tanggalPengajuan: Long = System.currentTimeMillis(),
    val jumlahPinjaman: String = "",
    val tenor: String = "",
    val status: String = "Berjalan",
    val sisaHutang: Double = 0.0,  // Sisa hutang yang harus dibayar
    val statusPembayaran: String = "Berjalan"  // Status pembayaran, misalnya "Berjalan", "Terverifikasi", "Selesai"
)