package com.myproject.safealarm.feature.guard.alarm

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.myproject.safealarm.App
import com.myproject.safealarm.R
import com.myproject.safealarm.service.alarmManagerReceiver
import com.myproject.safealarm.databinding.ActivityAlarmSetBinding
import java.util.*

class AlarmSetActivity : AppCompatActivity() {
    private val context = this

    private lateinit var binding: ActivityAlarmSetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmSetBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setTimeText()

        binding.add.setOnClickListener {
            val alarmDialog = AlarmCustomDialog(this)
            alarmDialog.show()

            alarmDialog.setOnClickedListener(object: AlarmCustomDialog.ButtonClickListener {
                override fun onClicked(hour: String, minute: String) {
                    addAlarm(0, App.prefs.alarmCount, hour, minute)
                    onResume()
                }
            })
        }

        binding.delBtn.setOnClickListener {
            delAlarm()
        }
    }


    override fun onResume() {
        super.onResume()
        setTimeText()
    }
}

class AlarmCustomDialog(val context: Context){
    private val dialog = Dialog(context)

    lateinit var okBtn: Button
    lateinit var cancelBtn: Button
    lateinit var hourSpin: Spinner
    lateinit var minSpin: Spinner

    fun show(){
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