package com.myproject.safealarm.feature.guard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.myproject.safealarm.feature.missing.info.MissingInfoActivity
import com.myproject.safealarm.databinding.ActivityGuardBinding
import com.myproject.safealarm.feature.guard.help.GuardHelpActivity
import com.myproject.safealarm.feature.guard.map.GuardMapActivity
import com.myproject.safealarm.feature.guard.setting.GuardSettingActivity

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