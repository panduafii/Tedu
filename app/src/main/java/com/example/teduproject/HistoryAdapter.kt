package com.example.teduproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teduproject.R

class HistoryAdapter(private val historyList: List<HistoryItem>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewStory: TextView = view.findViewById(R.id.textViewStory)
        val textViewDepression: TextView = view.findViewById(R.id.textViewDepression)
        val textViewAnxiety: TextView = view.findViewById(R.id.textViewAnxiety)
        val textViewPoints: TextView = view.findViewById(R.id.textViewPoints)
        val textViewSummary: TextView = view.findViewById(R.id.textViewSummary)
        val textViewStress: TextView = view.findViewById(R.id.textViewStress)
        val textViewTimestamp: TextView = view.findViewById(R.id.textViewTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = historyList[position]
        holder.textViewStory.text = item.cerita
        holder.textViewDepression.text = "Depression: ${item.depresi}"
        holder.textViewAnxiety.text = "Anxiety: ${item.kecemasan}"
        holder.textViewPoints.text = "Points: ${item.poin}"
        holder.textViewSummary.text = "Summary: ${item.rangkuman}"
        holder.textViewStress.text = "Stress: ${item.stress}"
        holder.textViewTimestamp.text = "Timestamp: ${item.timestamp}"
    }

    override fun getItemCount() = historyList.size
}

