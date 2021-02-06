package com.myproject.safealarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class GuardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guard)

        var btn = findViewById<Button>(R.id.help_btn)
        btn.setOnClickListener {
            ForegroundService
        }
    }
}