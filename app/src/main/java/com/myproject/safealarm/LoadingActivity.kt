package com.myproject.safealarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import java.util.Random
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        readPref()
    }

    private fun readPref(){
        Log.d("id생성", "${App.prefs.idOn}, ${App.prefs.id}")
        val isReg = App.prefs.regKey
        if(isReg){
            moveActivity()
        }else{
            if(!App.prefs.idOn){
                createId()
            }
            Log.d("액티비티 이동", "로딩->등록")
            startActivity(Intent(this, RegistActivity::class.java))

            finish()
        }
    }

    private fun createId(){
        var rNum = Random().nextInt(100000) + 1
        Singleton.server.fConnect(rNum.toString()).enqueue(object:Callback<ResponseDC>{
            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Log.d("에러", t.toString())
                createId()
            }
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                Log.d("접속", "${response.body()!!.result!!.toBoolean()}, ${rNum}")
            }
        })
        App.prefs.idOn = true
        App.prefs.id = rNum.toString()
    }
    private fun moveActivity(){
        val selectRole: String = App.prefs.role
        if(selectRole === "Guard"){
            Log.d("액티비티 이동", "로딩->보호자")
            startActivity(Intent(this, GuardActivity::class.java))
        }else{
            Log.d("액티비티 이동", "로딩->피보호자")
            startActivity(Intent(this, WardActivity::class.java))
        }
        finish()
    }
}