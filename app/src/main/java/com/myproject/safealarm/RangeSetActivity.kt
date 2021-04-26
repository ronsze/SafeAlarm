package com.myproject.safealarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.myproject.safealarm.databinding.ActivityRangeSetBinding

class RangeSetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRangeSetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRangeSetBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.add.setOnClickListener {
            startActivity(Intent(this, RangeAddActivity::class.java))
        }
    }
}