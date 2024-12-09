package com.example.teduproject

import android.graphics.Color
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity


class Kuisioner : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kuisioner)


        val radioGroup: RadioGroup = findViewById(R.id.radioGroup)


        radioGroup.setOnCheckedChangeListener { group, checkedId ->

            for (i in 0 until group.childCount) {
                val radioButton = group.getChildAt(i) as RadioButton
                if (radioButton.id == checkedId) {

                    radioButton.setTextColor(Color.BLUE)
                } else {

                    radioButton.setTextColor(Color.BLACK)
                }
            }
        }
    }
}
