package com.myproject.safealarm

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.myproject.safealarm.databinding.ActivityRegistBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistActivity : AppCompatActivity() {
    private val context = this
    private lateinit var binding: ActivityRegistBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.GuardBtn.setOnClickListener {
            showGuardDialog()
        }
        binding.WardBtn.setOnClickListener {
            showWardDialog()
        }

    }
    fun showGuardDialog(){
        val builder = AlertDialog.Builder(this)

        builder.setTitle("피보호자 화면에 입력해주세요.\n등록이 끝날때까지 창을 닫지마세요.")
        builder.setMessage("ID : ${App.prefs.id}")
        var dialog_listener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int){
                when(which){
                    DialogInterface.BUTTON_NEGATIVE -> {
                        registGuard()
                    }
                }
            }
        }
        builder.setNegativeButton("확인", dialog_listener)
        builder.setPositiveButton("취소", null)
        builder.show()
    }
    fun showWardDialog(){
        val builder = AlertDialog.Builder(this)
        val et = EditText(this)
        builder.setMessage("보호자 화면의 코드를 입력해주세요.")
        builder.setView(et)
        var dialog_listener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int){
                when(which){
                    DialogInterface.BUTTON_NEGATIVE -> {
                        registWard(et.text.toString())
                    }
                }
            }
        }
        builder.setNegativeButton("확인", dialog_listener)
        builder.setPositiveButton("취소", null)
        builder.show()
    }

    fun registGuard(){
        Singleton.server.registGuard(App.prefs.id).enqueue(object:Callback<ResponseDC>{
            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Log.d("보호자 등록", "실패")
            }
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                Log.d("보호자 등록", "성공")
                Toast.makeText(context, "등록되었습니다.", Toast.LENGTH_SHORT).show()
                App.prefs.regKey = true
                App.prefs.role = "Guard"
                App.prefs.room = App.prefs.id
                startForeService()
            }
        })
    }

    fun registWard(code: String){
        Singleton.server.registWard(App.prefs.id, code).enqueue(object:Callback<ResponseDC>{
            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Log.d("피보호자 등록", "실패")
                Toast.makeText(context, "보호자 id가 존재하지 않거나\n이미 등록된 사용자입니다.", Toast.LENGTH_SHORT).show()
            }
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                Log.d("피보호자 등록", "성공")
                Toast.makeText(context, "등록되었습니다.", Toast.LENGTH_SHORT).show()
                App.prefs.regKey = true
                App.prefs.role = "Ward"
                App.prefs.room = code
                startForeService()
            }
        })
    }

    fun startForeService(){
        val foreIntent = Intent(this@RegistActivity, ForegroundService::class.java)
        foreIntent.action = Actions.START_FOREGROUND
        Log.d("Foreground서비스", "Foreground서비스 시작")
        startService(foreIntent)
        moveActivity()
    }

    fun moveActivity(){
        startActivity(Intent(this, LoadingActivity::class.java))
        Log.d("액티비티 이동", "등록->로딩")
        finish()
    }
}