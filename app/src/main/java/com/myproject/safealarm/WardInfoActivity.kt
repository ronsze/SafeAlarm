package com.myproject.safealarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.myproject.safealarm.databinding.ActivityWardInfoBinding

class WardInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWardInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWardInfoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        spinnerSet()

        binding.save.setOnClickListener {
            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
        binding.cancle.setOnClickListener {
            finish()
        }
    }

    private fun spinnerSet(){
        val sAdapter = ArrayAdapter.createFromResource(this, R.array.sex, android.R.layout.simple_spinner_dropdown_item)

        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sexSpin.adapter = sAdapter
        binding.sexSpin.setSelection(0)
    }
}