package com.myproject.safealarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.myproject.safealarm.databinding.ActivityGuardBinding

class GuardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.locationBtn.setOnClickListener {
            startActivity(Intent(this, GuardMapActivity::class.java))
        }

        binding.helpBtn.setOnClickListener {
            startActivity(Intent(this, GuardHelpActivity::class.java))
        }

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(this, GuardSettingActivity::class.java))
        }

        binding.infoBtn.setOnClickListener {
            startActivity(Intent(this, MissingInfoActivity::class.java))
        }
    }
}