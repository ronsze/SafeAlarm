package com.myproject.safealarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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
        setTimeText()

        binding.add.setOnClickListener {
            startActivity(Intent(this, AlarmAddActivity::class.java))
        }

        binding.delBtn.setOnClickListener {
            var alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            var intent = Intent(this, alarmManagerReceiver::class.java)
            var pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

            alarmManager.cancel(pIntent)
            App.prefs.a_count = 0
            App.prefs.alarmTime = ""
            setTimeText()
        }
    }

    private fun setTimeText(){
        if(App.prefs.a_count == 0){
            binding.delBtn.visibility = View.INVISIBLE
            binding.alarmText.text = "등록된 알람이 없습니다."
            binding.alarmText.textSize = 20.0f
        }else if(App.prefs.a_count == 1){
            binding.delBtn.visibility = View.VISIBLE
            binding.alarmText.text = App.prefs.alarmTime
            binding.alarmText.textSize = 30.0f
        }
    }

    override fun onResume() {
        super.onResume()
        setTimeText()
        if(App.prefs.a_count == 1){
            binding.add.visibility = View.INVISIBLE
        }else{
            binding.add.visibility = View.VISIBLE
        }
    }
}