package com.myproject.safealarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.myproject.safealarm.databinding.ActivityGuardSettingBinding

class GuardSettingActivity : AppCompatActivity() {
    lateinit var binding: ActivityGuardSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardSettingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.rangeSet.setOnClickListener {
            startActivity(Intent(this, RangeSetActivity::class.java))
        }

        binding.wardInfo.setOnClickListener {
            startActivity(Intent(this, WardInfoActivity::class.java))
        }

        binding.alarmSet.setOnClickListener {
            startActivity(Intent(this, AlarmSetActivity::class.java))
        }
    }
}