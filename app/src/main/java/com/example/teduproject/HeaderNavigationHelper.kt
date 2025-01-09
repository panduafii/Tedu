package com.example.teduproject

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

object HeaderNavigationHelper {
    fun setupHeaderNavigation(context: Context, headerView: View) {
        val logo = headerView.findViewById<ImageView>(R.id.logo)

        logo.setOnClickListener {
            if (context !is MainActivity) {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)

                (context as? AppCompatActivity)?.overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }
        }
    }
}
