package com.example.teduproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class RewardItem(val name: String, val points: Int, val logo: String) // Define RewardItem class if not already defined

class RewardsAdapter(
    private val rewardsList: List<RewardItem>,
    private val onRedeemClicked: (RewardItem) -> Unit
) : RecyclerView.Adapter<RewardsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageReward: ImageView = view.findViewById(R.id.imageReward)
        val textRewardName: TextView = view.findViewById(R.id.textRewardName)
        val buttonRedeem: Button = view.findViewById(R.id.buttonRedeem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reward, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reward = rewardsList[position]
        holder.textRewardName.text = "${reward.name} - ${reward.points} Poin"

        // Set placeholder image
        holder.imageReward.setImageResource(
            holder.itemView.context.resources.getIdentifier(
                reward.logo,
                "drawable",
                holder.itemView.context.packageName
            )
        )

        // Handle Redeem button click
        holder.buttonRedeem.setOnClickListener {
            onRedeemClicked(reward)
        }
    }

    override fun getItemCount() = rewardsList.size
}


