package com.myproject.safealarm

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.myproject.safealarm.databinding.ActivityQRCodeBinding
import io.socket.client.IO
import io.socket.emitter.Emitter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigInteger
import java.util.*

class QRCodeActivity : AppCompatActivity() {
    private val context = this
    private lateinit var socketT: socketThread
    private lateinit var binding: ActivityQRCodeBinding
    val mSocket_R = App.mSocket
    private lateinit var p: BigInteger ; private lateinit var g: BigInteger ; private lateinit var x: BigInteger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQRCodeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        connectSocket()
        createQRCode()
    }

    private fun connectSocket() {
        socketT = socketThread()
        socketT.run()
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
            mSocket_R.emit("enterRoom", App.prefs.id)
            mSocket_R.on("receivePrime", onReceivePrime)
            mSocket_R.on("receiveR2", onReceiveR2)
        }
    }

    fun createQRCode(){
        val qrCode = QRCodeWriter()
        val bitMtx = qrCode.encode(
            "Guard." + intent.getStringExtra("id"),
            BarcodeFormat.QR_CODE,
            350,
            350
        )
        val bitmap: Bitmap = Bitmap.createBitmap(bitMtx.width, bitMtx.height, Bitmap.Config.RGB_565)
        for(i in 0 .. bitMtx.width-1){
            for(j in 0 .. bitMtx.height-1){
                var color = 0
                if(bitMtx.get(i, j)){
                    color = Color.BLACK
                }else{
                    color = Color.WHITE
                }
                bitmap.setPixel(i, j, color)
            }
        }
        binding.qrImage.setImageBitmap(bitmap)
    }

    fun registGuard(){                          //보호자 등록
        Singleton.server.registGuard(App.prefs.id).enqueue(object: Callback<ResponseDC> {
            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Toast.makeText(context, "다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                Toast.makeText(context, "등록되었습니다.", Toast.LENGTH_SHORT).show()
                App.prefs.regKey = true
                App.prefs.role = "Guard"
                App.prefs.room = App.prefs.id
                startForeService()
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

    fun backActivity(){
        val intent = Intent(this, RegistActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        Log.e("backpressed", "OnBackPressed")
        backActivity()
        super.onBackPressed()
    }

    private val onReceivePrime = Emitter.Listener{
        var primeStr = it[0].toString()
        var arr = primeStr.split(".")
        this.p = arr[0].toBigInteger()
        this.g = arr[1].toBigInteger()
        var x = BigInteger(1024, Random())
        while(x > p.subtract(1.toBigInteger())){
            x = BigInteger(1024, Random())
        }
        this.x = x
        var r1 = g.modPow(x, p)
        mSocket_R.emit("sendR1", r1.toString())
    }

    private val onReceiveR2 = Emitter.Listener {
        val r2 = it[0].toString().toBigInteger()
        var key = r2.modPow(x, p)
        App.prefs.key = key.toString()
        registGuard()
    }
}