package com.myproject.safealarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.myproject.safealarm.databinding.ActivityGuardBinding

class GuardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.mapLayout.setOnClickListener {
            startActivity(Intent(this, GuardMapActivity::class.java))
        }

        binding.helpLayout.setOnClickListener {
            startActivity(Intent(this, GuardHelpActivity::class.java))
        }

        binding.setLayout.setOnClickListener {
            startActivity(Intent(this, GuardSettingActivity::class.java))
        }

        binding.infoLayout.setOnClickListener {
            startActivity(Intent(this, MissingInfoActivity::class.java))
        }
    }
}