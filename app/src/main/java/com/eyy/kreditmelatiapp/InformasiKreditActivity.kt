package com.eyy.kreditmelatiapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class InformasiKreditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informasi_kredit)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = KreditAdapter(getKreditList())
    }

    private fun getKreditList(): List<Kredit> {
        return listOf(
            Kredit("Kredit Usaha", "Kredit untuk pengembangan usaha kecil", "Syarat: bla bla bla", "Profil: bla bla bla"),
            Kredit("Kredit Pendidikan", "Kredit untuk biaya pendidikan", "Syarat: bla bla bla", "Profil: bla bla bla"),
            // Tambahkan produk kredit lainnya
        )
    }
}

data class Kredit(val nama: String, val deskripsi: String, val syarat: String, val profil: String)

class KreditAdapter(private val kreditList: List<Kredit>) : RecyclerView.Adapter<KreditAdapter.KreditViewHolder>() {

    class KreditViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val namaTextView: TextView = view.findViewById(R.id.namaTextView)
        val deskripsiTextView: TextView = view.findViewById(R.id.deskripsiTextView)
        val syaratTextView: TextView = view.findViewById(R.id.syaratTextView)
        val profilTextView: TextView = view.findViewById(R.id.profilTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KreditViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kredit, parent, false)
        return KreditViewHolder(view)
    }

    override fun onBindViewHolder(holder: KreditViewHolder, position: Int) {
        val kredit = kreditList[position]
        holder.namaTextView.text = kredit.nama
        holder.deskripsiTextView.text = kredit.deskripsi
        holder.syaratTextView.text = kredit.syarat
        holder.profilTextView.text = kredit.profil
    }

    override fun getItemCount() = kreditList.size
}
