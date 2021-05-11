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

class QRCodeActivity : AppCompatActivity() {
    private val context = this
    private lateinit var socketT: socketThread
    private lateinit var binding: ActivityQRCodeBinding


    companion object {
        val mSocket_R = App.mSocket
        const val NOTIFICATION_ID = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQRCodeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        connectSocket()
        mSocket_R.emit("enterRoom", App.prefs.id)
        createQRCode()

        binding.okBtn.setOnClickListener {

        }
        binding.cancleBtn.setOnClickListener {
            Toast.makeText(context, "취소되었습니다.", Toast.LENGTH_SHORT).show()
            onBackPressed()
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
                mSocket_R.on("regist_G", onRegistG)
                mSocket_R.on("finish", onFinish)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createQRCode(){
        val qrCode = QRCodeWriter()
        val bitMtx = qrCode.encode(
            intent.getStringExtra("key"),
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
        Log.e("키는뭐노", App.prefs.key)
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
        super.onBackPressed()
        backActivity()
    }

    private val onRegistG = Emitter.Listener{
        Log.e("regist_G", "받음")
        val arr = it[0].toString().split(".")
        val code = arr[0]
        val w_id = arr[1]
        if(code == App.prefs.id){
            mSocket_R.emit("ok_G", w_id)
        }else{
            Log.e("Regist_G", "에러")
        }
    }

    private val onFinish = Emitter.Listener {
        Log.e("onFinish", "받음")
        val code = it[0].toString()
        if(code == App.prefs.id){
            registGuard()
        }else{
            Log.e("finish", "에러")
        }
    }
}