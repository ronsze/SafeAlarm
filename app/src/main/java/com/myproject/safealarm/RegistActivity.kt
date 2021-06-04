package com.myproject.safealarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.util.Base64Utils
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.myproject.safealarm.databinding.ActivityRegistBinding
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

class RegistActivity : AppCompatActivity() {
    private val context = this
    private val mSocketR = App.mSocket

    private lateinit var binding: ActivityRegistBinding
    private lateinit var socketT: socketThread
    private lateinit var remoteID: String
    private lateinit var p: BigInteger ; private lateinit var g: BigInteger
    private lateinit var loadingDlog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        loadingDlog = LoadingDialog(this)

        binding.guardLayout.setOnClickListener {
            val qrIntent = Intent(this, QRCodeActivity::class.java)
            qrIntent.putExtra("id", App.prefs.id)
            startActivity(qrIntent)
            finish()
        }
        binding.wardLayout.setOnClickListener {
            connectSocket()
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
                if(!mSocketR.connected()){
                    mSocketR.connect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mSocketR.on("receiveR1", onReceiveR1)
            mSocketR.on("callbackPrime", onCallbackPrime)
        }
    }

    private fun scanQRCode(){
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
                Log.e("QR스캔", "contents 빔.")
                finish()
            } else {
                val QRArr = result.contents.split(".")
                if (QRArr[0] != "Guard") {
                    Log.e("QR스캔", "잘못된 QR코드.")
                    finish()
                } else {
                    val remoteID = QRArr[1]
                    this.remoteID = remoteID
                    startDHExchange(remoteID)
                }
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun startDHExchange(remoteID: String){
        loadingDlog.show()
        try{
            mSocketR.emit("enterRoom", remoteID)
            mSocketR.emit("getPrime", App.prefs.id)    //onCallbackPrime
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private val onCallbackPrime = Emitter.Listener {
        Singleton.server.getCert(this.remoteID).enqueue(object:Callback<ResponseDC>{
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                val certificate = response.body()!!.result!!

                Singleton.server.getCRL().enqueue(object: Callback<ResponseDC>{
                    override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                        val path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                        val crl = loadCRL(response.body()!!.result!!, path)
                        saveCertificate(certificate, path, crl) //result = X509certificate
                        val primeNumber = it[0].toString()
                        sendPrime(primeNumber)
                    }

                    override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                        Log.e("인증서", "실패")
                    }
                })
            }
            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Log.e("인증서", "실패")
            }
        })
    }

    private fun sendPrime(primeNumber: String){
        val primeNum = primeNumber.substring(2, primeNumber.length - 2)
        val arr = primeNum.split(".")
        this.p = arr[0].toBigInteger()
        this.g = arr[1].toBigInteger()
        mSocketR.emit("sendPrime", App.prefs.id+"9y6s0y9"+primeNum+getSign(primeNum))
    }

    private val onReceiveR1 = Emitter.Listener {
        try {
            val msg = it[0].toString()
            val r1 = msg.split("SiGn")[0].toBigInteger()
            if(checkSign(msg, getPublicKey())){
                val y = getY()
                saveShardKey(r1, y)
                sendR2(y)
                registWard()
            }else{
                Log.e("서명 receiveR1", "서명 불일치")
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun getY(): BigInteger{
        var y = BigInteger(1024, Random())
        while(y > p.subtract(1.toBigInteger())){
            y = BigInteger(1024, Random())
        }
        return y
    }

    private fun saveShardKey(r1: BigInteger, y: BigInteger){
        val shardKey = r1.modPow(y, p)
        App.prefs.shardKey = shardKey.toString()
    }

    private fun sendR2(y: BigInteger){
        val r2 = g.modPow(y, p)
        mSocketR.emit("sendR2", r2.toString()+getSign(r2.toString()))
    }

    private fun registWard(){                  //피보호자 등록
        val remoteID = this.remoteID
        Singleton.server.registWard(App.prefs.id, remoteID).enqueue(object:Callback<ResponseDC>{
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                App.prefs.regKey = true
                App.prefs.role = "Ward"
                App.prefs.room = remoteID
                startForeService()
            }
            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Log.d("피보호자 등록", "실패")
                Toast.makeText(context, "보호자 id가 존재하지 않거나\n이미 등록된 사용자입니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun startForeService(){                 //Foregroud서비스 시작
        val foreIntent = Intent(this, ForegroundService::class.java)
        foreIntent.action = Actions.START_FOREGROUND
        startService(foreIntent)
        moveLoadingActivity()
    }

    fun moveLoadingActivity(){                     //액티비티 이동
        startActivity(Intent(this, LoadingActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        loadingDlog.dismiss()
        super.onDestroy()
    }
}