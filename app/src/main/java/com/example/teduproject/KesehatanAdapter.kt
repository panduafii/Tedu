package com.example.teduproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class KesehatanAdapter(private val data: List<UserData>) :
    RecyclerView.Adapter<KesehatanAdapter.KesehatanViewHolder>() {

    class KesehatanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvUmur: TextView = itemView.findViewById(R.id.tvUmur)
        val tvJenisKelamin: TextView = itemView.findViewById(R.id.tvJenisKelamin)
        val tvAlergi: TextView = itemView.findViewById(R.id.tvAlergi)
        val tvKondisiKesehatan: TextView = itemView.findViewById(R.id.tvKondisiKesehatan)
        val tvPantangan: TextView = itemView.findViewById(R.id.tvPantangan)
        val tvBeratBadan: TextView = itemView.findViewById(R.id.tvBeratBadan)
        val tvTinggiBadan: TextView = itemView.findViewById(R.id.tvTinggiBadan)
        val tvKondisiUmum: TextView = itemView.findViewById(R.id.tvKondisiUmum)
        val tvMakananKonsumsi: TextView = itemView.findViewById(R.id.tvMakananKonsumsi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KesehatanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kesehatan, parent, false)
        return KesehatanViewHolder(view)
    }

    override fun onBindViewHolder(holder: KesehatanViewHolder, position: Int) {
        val item = data[position]
        holder.tvNama.text = "Nama: ${item.nama}"
        holder.tvUmur.text = "Umur: ${item.umur}"
        holder.tvJenisKelamin.text = "Jenis Kelamin: ${item.jenis_kelamin}"
        holder.tvAlergi.text = "Alergi Makanan: ${item.alergi.ifEmpty { "Tidak ada" }}"
        holder.tvKondisiKesehatan.text = "Kondisi Kesehatan: ${item.kondisi_kesehatan.joinToString(", ").ifEmpty { "Sehat" }}"
        holder.tvPantangan.text = "Pantangan Makanan: ${item.pantangan.ifEmpty { "Tidak ada" }}"
        holder.tvBeratBadan.text = "Berat Badan: ${item.berat_badan} kg"
        holder.tvTinggiBadan.text = "Tinggi Badan: ${item.tinggi_badan} cm"
        holder.tvKondisiUmum.text = "Kondisi Umum: ${item.kondisi_umum}"
        holder.tvMakananKonsumsi.text = "Makanan yang Dikonsumsi: ${item.makanan_konsumsi}"
    }

    override fun getItemCount(): Int = data.size
}
