package com.myproject.safealarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.myproject.safealarm.databinding.ActivityAlarmAddBinding
import com.myproject.safealarm.databinding.ActivityRangeAddBinding

class AlarmAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmAddBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        spinnerSet()

        binding.add.setOnClickListener {
            Toast.makeText(this, "${binding.hourSpin.selectedItem.toString()}시, ${binding.minSpin.selectedItem.toString()}분", Toast.LENGTH_SHORT).show()
        }
        binding.cancle.setOnClickListener {
            finish()
        }
    }

    private fun spinnerSet(){
        val hAdapter = ArrayAdapter.createFromResource(this, R.array.hour, R.layout.spinner_font)
        val mAdapter = ArrayAdapter.createFromResource(this, R.array.min, R.layout.spinner_font)

        hAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.hourSpin.adapter = hAdapter
        binding.hourSpin.setSelection(0)
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.minSpin.adapter = mAdapter
        binding.minSpin.setSelection(0)
    }
}