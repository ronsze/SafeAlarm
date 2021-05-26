package com.myproject.safealarm

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.myproject.safealarm.databinding.ActivityWardSettingBinding

class WardSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWardSettingBinding
    private val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWardSettingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.certiPass.setOnClickListener {
            if(App.prefs.cert){
                showWardDialog()
            }else{
                Toast.makeText(context, "보호자로부터의 인증요청이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.setPass.setOnClickListener {
            showSetPassDialog(this)
        }
    }

    fun showWardDialog() {                       //피보호자 다이어로그
        val builder = AlertDialog.Builder(this)
        val et = EditText(this)
        builder.setMessage("패스워드를 입력하세요.")
        builder.setView(et)
        var dialog_listener = object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                when (which) {
                    DialogInterface.BUTTON_NEGATIVE -> {
                        if (et.text.toString() == App.prefs.pass) {
                            Toast.makeText(context, "인증되었습니다.", Toast.LENGTH_SHORT).show()
                            App.mSocket.emit("callbackCert")
                            App.prefs.cert = false
                        } else {
                            Toast.makeText(context, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        builder.setNegativeButton("확인", dialog_listener)
        builder.setPositiveButton("취소", null)
        builder.show()
    }

    fun showSetPassDialog(context: Context){
        val builder = AlertDialog.Builder(this)
        val et = EditText(this)
        builder.setTitle("설정할 패스워드를 입력하세요")
        if(App.prefs.pass != "0000"){
            builder.setMessage("이미 등록된 패스워드가 있습니다.")
        }else{
            builder.setMessage("등록된 패스워드가 없습니다.\n기본 패스워드는 0000입니다.")
        }
        builder.setView(et)
        var dialog_listener = object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                when(which){
                    DialogInterface.BUTTON_NEGATIVE -> {
                        if (et.text.toString() != "") {
                            App.prefs.pass = et.text.toString()
                        }
                        Toast.makeText(context, "등록되었습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
        builder.setNegativeButton("확인", dialog_listener)
        builder.setPositiveButton("취소", null)
        builder.show()
    }
}

