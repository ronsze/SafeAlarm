package com.myproject.safealarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.myproject.safealarm.databinding.ActivityAlarmSetBinding
import java.util.*

class AlarmSetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmSetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmSetBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.add.setOnClickListener {
            startActivity(Intent(this, AlarmAddActivity::class.java))
        }
    }
}