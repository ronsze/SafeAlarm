package com.myproject.safealarm

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.myproject.safealarm.databinding.ActivityRegistBinding
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class RegistActivity : AppCompatActivity() {
    private val context = this
    private lateinit var binding: ActivityRegistBinding
    private var keyStr: String = ""
    private lateinit var socketT: socketThread
    private lateinit var room: String

    companion object {
        val mSocket_R = App.mSocket
        const val NOTIFICATION_ID = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.GuardBtn.setOnClickListener {
            var qrIntent = Intent(this, QRCodeActivity::class.java)
            qrIntent.putExtra("key", createKeyStr())
            startActivity(qrIntent)
        }
        binding.WardBtn.setOnClickListener {
            scanQRCode()
        }
    }

    private fun connectSocket() {
        socketT = socketThread()
        socketT.run()
    }

    inner class socketThread : Thread() {
        override fun run() {
            try {
                mSocket_R.connect()
                mSocket_R.on("ok_W", onOK)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createKeyStr(): String{
        var str = "Guard."
        val keygen = KeyGenerator.getInstance("AES")
        keygen.init(256)
        val key: SecretKey = keygen.generateKey()
        str += App.prefs.id + "."
        for(i in 0 .. key.encoded.size-1){
            keyStr += key.encoded[i].toString() + ","
        }
        App.prefs.key = keyStr
        str += keyStr
        return str
    }

    fun scanQRCode(){
        val integrator = IntentIntegrator(this)
        integrator.setBeepEnabled(false)
        integrator.setOrientationLocked(true)
        integrator.setPrompt("QR코드를 찍어주세요.")
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if(result != null) {
            if (result.contents == null) {
                Log.e("this", "잘못된 QR코드입니다.")
                finish()
            } else {
                val QRArr = result.contents.split(".")
                if (QRArr[0] != "Guard") {
                    Log.e("this", "잘못된 QR코드입니다.")
                    finish()
                } else {
                    connectSocket()
                    mSocket_R.emit("enterRoom", App.prefs.id)
                    Log.e("this", result.contents)
                    var code = QRArr[1]
                    keyStr = QRArr[2]
                    App.prefs.key = keyStr
                    registWard(code)
                }
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun registWard(code: String){                  //피보호자 등록
        Log.e("code", code)
        Singleton.server.registWard(App.prefs.id, code).enqueue(object:Callback<ResponseDC>{
            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Log.d("피보호자 등록", "실패")
                Toast.makeText(context, "보호자 id가 존재하지 않거나\n이미 등록된 사용자입니다.", Toast.LENGTH_SHORT).show()
            }
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                Log.d("피보호자 등록", "성공")
                mSocket_R.emit("regist_W", code + "." + App.prefs.id)
                room = code
            }
        })
    }

    fun startForeService(){                 //Foregroud서비스 시작
        val foreIntent = Intent(this, ForegroundService::class.java)
        foreIntent.action = Actions.START_FOREGROUND
        startService(foreIntent)
        moveActivity()
    }

    fun moveActivity(){                     //액티비티 이동
        startActivity(Intent(this, LoadingActivity::class.java))
        finish()
    }

    private val onOK = Emitter.Listener {
        try {
            Log.e("onOK", "받음")
            val id = it[0].toString()
            if (id == App.prefs.id) {
                App.prefs.regKey = true
                App.prefs.role = "Ward"
                App.prefs.room = room
                mSocket_R.emit("finish", room)
                startForeService()
            } else {
                Log.e("onOK", "에러")
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}