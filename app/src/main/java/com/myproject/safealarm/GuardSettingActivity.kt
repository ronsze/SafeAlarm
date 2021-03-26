package com.myproject.safealarm

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

        }
    }
}