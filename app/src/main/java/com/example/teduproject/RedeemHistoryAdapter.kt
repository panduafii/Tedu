package com.example.teduproject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.BaseAdapter

data class RedeemHistoryItem(
    val reward: String,
    val cost: Int,
    val timestamp: String
)

class RedeemHistoryAdapter(
    private val context: Context,
    private val items: List<RedeemHistoryItem>
) : BaseAdapter() {

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_history_redeem, parent, false)

        val item = items[position]

        val rewardName = view.findViewById<TextView>(R.id.rewardName)
        val rewardCost = view.findViewById<TextView>(R.id.rewardCost)
        val rewardTimestamp = view.findViewById<TextView>(R.id.rewardTimestamp)

        rewardName.text = "Hadiah: ${item.reward}"
        rewardCost.text = "Poin: ${item.cost}"
        rewardTimestamp.text = "Waktu: ${item.timestamp}"

        return view
    }
}
