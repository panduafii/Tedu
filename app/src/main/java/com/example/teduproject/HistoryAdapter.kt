package com.example.teduproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teduproject.R
import io.noties.markwon.Markwon

class HistoryAdapter(private val historyList: List<HistoryItem>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewStory: TextView = view.findViewById(R.id.textViewStory)
        val textViewDepression: TextView = view.findViewById(R.id.textViewDepression)
        val textViewAnxiety: TextView = view.findViewById(R.id.textViewAnxiety)
        val textViewStress: TextView = view.findViewById(R.id.textViewStress)
        val textViewPoints: TextView = view.findViewById(R.id.textViewPoints)
        val textViewSummary: TextView = view.findViewById(R.id.textViewSummary)
        val textViewTimestamp: TextView = view.findViewById(R.id.textViewTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = historyList[position]
        val markwon = Markwon.create(holder.itemView.context)

        // Gunakan Markwon untuk merender teks Markdown
        markwon.setMarkdown(holder.textViewStory, item.cerita ?: "")
        markwon.setMarkdown(holder.textViewDepression, "Depression: ${item.depresi ?: ""}")
        markwon.setMarkdown(holder.textViewAnxiety, "Anxiety: ${item.kecemasan ?: ""}")
        markwon.setMarkdown(holder.textViewStress, "Stress: ${item.stress ?: ""}")
        markwon.setMarkdown(holder.textViewPoints, "Points: ${item.poin ?: ""}")
        markwon.setMarkdown(holder.textViewSummary, "Summary: ${item.rangkuman ?: ""}")
        markwon.setMarkdown(holder.textViewTimestamp, "Timestamp: ${item.timestamp ?: ""}")
    }


    override fun getItemCount() = historyList.size
}

