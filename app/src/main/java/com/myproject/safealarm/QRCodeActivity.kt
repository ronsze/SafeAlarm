package com.myproject.safealarm

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.util.Base64Utils
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.myproject.safealarm.databinding.ActivityQRCodeBinding
import io.socket.emitter.Emitter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.math.BigInteger
import java.security.*
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.X509EncodedKeySpec
import java.util.*

class QRCodeActivity : AppCompatActivity() {
    private val context = this
    private val mSocketR = App.mSocket

    private lateinit var socketT: socketThread
    private lateinit var binding: ActivityQRCodeBinding
    private lateinit var p: BigInteger ; private lateinit var g: BigInteger ; private lateinit var x: BigInteger
    private lateinit var loadingDlog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQRCodeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        loadingDlog = LoadingDialog(this)

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
                if(!mSocketR.connected()){
                    mSocketR.connect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mSocketR.emit("enterRoom", App.prefs.id)
            mSocketR.on("receivePrime", onReceivePrime)
            mSocketR.on("receiveR2", onReceiveR2)
        }
    }

    fun createQRCode(){
        val qrCodeWriter = QRCodeWriter()
        val bitMtx = qrCodeWriter.encode(
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

    private val onReceivePrime = Emitter.Listener{
        var msg = it[0].toString().split("9y6s0y9")
        var remoteID = msg[0]
        val primeMsg = msg[1]
        Singleton.server.getCert(remoteID).enqueue(object:Callback<ResponseDC>{
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                val path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                saveCertificate(response.body()!!.result!!, path)
                sendR1(primeMsg)
            }
            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Log.e("인증서", "실패")
            }
        })
    }

    private fun sendR1(primeMsg: String){
        val primeNum = primeMsg.split("SiGn")[0]
        if(checkSign(primeMsg)){
            val primeArr = primeNum.split(".")
            this.p = primeArr[0].toBigInteger()
            this.g = primeArr[1].toBigInteger()
            this.x = getX()
            val r1 = g.modPow(x, p)
            mSocketR.emit("sendR1", r1.toString()+getSign(r1.toString()))
        }else{
            Log.e("서명 sendR1", "서명 불일치")
        }
    }

    private fun getX(): BigInteger{
        var x = BigInteger(1024, Random())
        while(x > p.subtract(1.toBigInteger())){
            x = BigInteger(1024, Random())
        }
        return x
    }

    private val onReceiveR2 = Emitter.Listener {
        val msg = it[0].toString()
        val r2 = msg.split("SiGn")[0].toBigInteger()
        if(checkSign(msg)){
            saveShardKey(r2, x)
            registGuard()
        }else{
            Log.e("서명 receiveR2", "서명 불일치")
        }
    }

    private fun saveShardKey(r2: BigInteger, x: BigInteger){
        val shardKey = r2.modPow(x, p)
        App.prefs.shardKey = shardKey.toString()
    }

    fun registGuard(){                          //보호자 등록
        Singleton.server.registGuard(App.prefs.id).enqueue(object: Callback<ResponseDC> {
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                Toast.makeText(context, "등록되었습니다.", Toast.LENGTH_SHORT).show()
                App.prefs.regKey = true
                App.prefs.role = "Guard"
                App.prefs.room = App.prefs.id
                startForeService()
            }

            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Toast.makeText(context, "다시 시도해주세요.", Toast.LENGTH_SHORT).show()
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
        backActivity()
        super.onBackPressed()
    }
}