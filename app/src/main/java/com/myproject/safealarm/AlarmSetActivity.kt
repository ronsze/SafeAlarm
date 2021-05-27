package com.myproject.safealarm

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.myproject.safealarm.databinding.ActivityAlarmSetBinding
import com.myproject.safealarm.databinding.AddAlarmDialogBinding
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
            val dialog = AlarmCustomDialog(this)
            dialog.myDig()

            dialog.setOnClickedListener(object: AlarmCustomDialog.ButtonClickListener{
                override fun onClicked(hour: String, minute: String) {
                    addAlarm(0, App.prefs.a_count, hour, minute)
                    onResume()
                }
            })
        }

        binding.delBtn.setOnClickListener {
            var alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            var intent = Intent(this, alarmManagerReceiver::class.java)
            var pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

            alarmManager.cancel(pIntent)
            App.prefs.a_count = 0
            App.prefs.alarmTime = ""

            Toast.makeText(this, "제거되었습니다.", Toast.LENGTH_SHORT).show()

            onResume()
        }
    }

    private fun setTimeText(){
        if(App.prefs.a_count == 0){
            binding.delBtn.visibility = View.INVISIBLE
            binding.alarmText.text = "등록된 알람이 없습니다."
            binding.alarmText.textSize = 25.0f
        }else if(App.prefs.a_count == 1){
            binding.delBtn.visibility = View.VISIBLE
            binding.alarmText.text = App.prefs.alarmTime
            binding.alarmText.textSize = 25.0f
        }
    }

    fun addAlarm(add: Int, requestNum: Int, tHour: String, tMin: String){
        var alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var intent = Intent(this, alarmManagerReceiver::class.java)
        intent.putExtra("num", requestNum)
        intent.putExtra("hour", tHour)
        intent.putExtra("min", tMin)
        var pIntent = PendingIntent.getBroadcast(this, requestNum, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        var day = Date(System.currentTimeMillis()).date
        var hour = tHour.toInt()
        val minute = tMin.toInt()
        App.prefs.alarmTime = "${hour}시   ${minute}분"

        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, day+add)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)

        if(hour-9 < 0){
            day -= 1
            hour += 24
            cal.set(Calendar.DAY_OF_MONTH, day+add)
            cal.set(Calendar.HOUR_OF_DAY, hour)
        }

        Log.e("씨발", "${day} ${day+add}, ${hour}, ${minute}")
        Log.e("알람1", cal.timeInMillis.toString())
        Log.e("알람2", System.currentTimeMillis().toString())
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pIntent)
        App.prefs.a_count = 1
        Toast.makeText(this, "등록되었습니다.", Toast.LENGTH_SHORT).show()
    }

    inner class reAlarm: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent != null){
                val hour = intent.getStringExtra("hour")
                val min = intent.getStringExtra("min")
                val requestNum = intent.getIntExtra("num", 999999)
                addAlarm(1, requestNum, hour!!, min!!)
            }
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

class AlarmCustomDialog(val context: Context){
    private val dialog = Dialog(context)

    lateinit var okBtn: Button
    lateinit var cancelBtn: Button
    lateinit var hourSpin: Spinner
    lateinit var minSpin: Spinner

    fun myDig(){
        dialog.setContentView(R.layout.add_alarm_dialog)

        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        okBtn = dialog.findViewById<Button>(R.id.ok_btn)
        cancelBtn = dialog.findViewById<Button>(R.id.cancel_btn)
        hourSpin = dialog.findViewById<Spinner>(R.id.hour_spin)
        minSpin = dialog.findViewById<Spinner>(R.id.min_spin)

        spinnerSet(context)

        okBtn.setOnClickListener {
            onClickedListener.onClicked(hourSpin.selectedItem.toString(), minSpin.selectedItem.toString())
            dialog.dismiss()
        }
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    
    interface ButtonClickListener{
        fun onClicked(hour: String, minute: String)
    }

    private lateinit var onClickedListener: ButtonClickListener

    fun setOnClickedListener(listener: ButtonClickListener){
        onClickedListener = listener
    }

    private fun spinnerSet(context: Context){
        val hAdapter = ArrayAdapter.createFromResource(context, R.array.hour, R.layout.spinner_font)
        val mAdapter = ArrayAdapter.createFromResource(context, R.array.min, R.layout.spinner_font)

        hAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        hourSpin.adapter = hAdapter
        hourSpin.setSelection(0)
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        minSpin.adapter = mAdapter
        minSpin.setSelection(0)
    }
}