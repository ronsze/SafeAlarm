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
import java.math.BigInteger
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class RegistActivity : AppCompatActivity() {
    private val context = this
    private lateinit var binding: ActivityRegistBinding
    private var keyStr: String = ""
    private lateinit var socketT: socketThread
    private lateinit var room: String
    val mSocket_R = App.mSocket
    private lateinit var p: BigInteger ; private lateinit var g: BigInteger
    private var primeStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.GuardBtn.setOnClickListener {
            var qrIntent = Intent(this, QRCodeActivity::class.java)
            qrIntent.putExtra("id", App.prefs.id)
            startActivity(qrIntent)
            finish()
        }
        binding.WardBtn.setOnClickListener {
            connectSocket()
            scanQRCode()
        }
    }

    private fun connectSocket() {
        socketT = socketThread()
        socketT.run()
        Log.e("소켓켓", mSocket_R.connected().toString())
    }

    inner class socketThread : Thread() {
        override fun run() {
            try {
                if(!mSocket_R.connected()){
                    mSocket_R.connect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mSocket_R.on("receiveR1", onReceiveR1)
            mSocket_R.on("callbackPrime", onCallbackPrime)
        }
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
                    Log.e("this", "잘못된 QR코드입니다.2")
                    finish()
                } else {
                    var code = QRArr[1]
                    Log.e("code", code)
                    mSocket_R.emit("enterRoom", code)
                    mSocket_R.emit("getPrime", App.prefs.id)
                    registWard(code)
                }
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun registWard(code: String){                  //피보호자 등록
        Singleton.server.registWard(App.prefs.id, code).enqueue(object:Callback<ResponseDC>{
            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Log.d("피보호자 등록", "실패")
                Toast.makeText(context, "보호자 id가 존재하지 않거나\n이미 등록된 사용자입니다.", Toast.LENGTH_SHORT).show()
            }
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                Log.d("피보호자 등록", "성공")
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

    private val onCallbackPrime = Emitter.Listener {
        var primeStr = it[0].toString()
        primeStr = primeStr.substring(2, primeStr.length - 2)
        val arr = primeStr.split(".")
        this.primeStr = primeStr
        this.p = arr[0].toBigInteger()
        this.g = arr[1].toBigInteger()
        mSocket_R.emit("sendPrime", primeStr)
    }

    private val onReceiveR1 = Emitter.Listener {
        try {
            val r1 = it[0].toString().toBigInteger()
            var y = BigInteger(1024, Random())
            while(y > p.subtract(1.toBigInteger())){
                y = BigInteger(1024, Random())
            }
            val r2 = g.modPow(y, p)
            val key = r1.modPow(y, p)
            App.prefs.key = key.toString()
            App.prefs.regKey = true
            App.prefs.role = "Ward"
            App.prefs.room = room
            mSocket_R.emit("sendR2", r2.toString())
            startForeService()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}