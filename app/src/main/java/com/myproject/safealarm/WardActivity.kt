package com.myproject.safealarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.myproject.safealarm.databinding.ActivityGuardBinding
import com.myproject.safealarm.databinding.ActivityWardBinding

class WardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.helpBtn.setOnClickListener {
            val intent = Intent(this, ForegroundService::class.java)
            intent.action = Actions.HELP_CALL_WARD
            startService(intent)
        }

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(this, WardSettingActivity::class.java))
        }

        binding.infoBtn.setOnClickListener {
            startActivity(Intent(this, WardInfoActivity::class.java))
        }
    }
}