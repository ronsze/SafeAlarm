package com.myproject.safealarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.myproject.safealarm.databinding.ActivityAlarmAddBinding
import com.myproject.safealarm.databinding.ActivityRangeAddBinding
import java.util.*

class AlarmAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmAddBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        spinnerSet()

        binding.add.setOnClickListener {
            addAlarm(0, App.prefs.a_count)
            finish()
        }
        binding.cancle.setOnClickListener {
            finish()
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(reAlarm(), IntentFilter("alarm"))
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

    fun addAlarm(add: Int, requestNum: Int){
        var alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var intent = Intent(this, alarmManagerReceiver::class.java)
        intent.putExtra("num", requestNum)
        var pIntent = PendingIntent.getBroadcast(this, requestNum, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        var day = Date(System.currentTimeMillis()).date
        var hour = binding.hourSpin.selectedItem.toString().toInt()-9
        val minute = binding.minSpin.selectedItem.toString().toInt()
        App.prefs.alarmTime = "${hour+9}시   ${minute}분"

        if(hour < 0){
            day -= 1
            hour += 24
        }
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, day+add)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)

        if(add == 0 && cal.timeInMillis <= System.currentTimeMillis()){
            cal.set(Calendar.DAY_OF_MONTH, day+add+1)
        }
        Log.e("알람1", cal.timeInMillis.toString())
        Log.e("알람2", System.currentTimeMillis().toString())
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pIntent)
        App.prefs.a_count = 1
        Toast.makeText(this, "등록되었습니다.", Toast.LENGTH_SHORT).show()
    }

    inner class reAlarm: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent != null){
                addAlarm(1, intent.getIntExtra("num", 999999))
            }
        }
    }
}