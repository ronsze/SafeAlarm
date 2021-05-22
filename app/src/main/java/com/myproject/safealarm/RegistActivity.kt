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
import java.lang.RuntimeException
import java.math.BigInteger
import java.security.*
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

class RegistActivity : AppCompatActivity() {
    private val context = this
    private lateinit var binding: ActivityRegistBinding
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
                    this.room = code
                    try{
                        mSocket_R.emit("enterRoom", code)
                        mSocket_R.emit("getPrime", App.prefs.id)
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
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
        Singleton.server.getCert(room).enqueue(object:Callback<ResponseDC>{
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                getCertificate(response.body()!!.result!!)
                sendPrime(it[0].toString())
            }
            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Log.e("인증서", "실패")
            }
        })
    }

    private fun sendPrime(input: String){
        var primeStr = input
        primeStr = primeStr.substring(2, primeStr.length - 2)
        val arr = primeStr.split(".")
        this.primeStr = primeStr
        this.p = arr[0].toBigInteger()
        this.g = arr[1].toBigInteger()
        mSocket_R.emit("sendPrime", App.prefs.id+"9y6s0y9"+primeStr+getSign(primeStr))
        Log.e("넣을게", primeStr+getSign(primeStr))
    }


    private val onReceiveR1 = Emitter.Listener {
        try {
            val msg = it[0].toString().split("SiGn")
            val r1 = msg[0].toBigInteger()
            if(verifSign(it[0].toString())){
                Log.e("서명 검증1", "서명 일치")
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
                mSocket_R.emit("sendR2", r2.toString()+getSign(r2.toString()))
                startForeService()
            }else{
                Log.e("서명 검증1", "서명 불일치")
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun getSign(input: String): String{
        val hash: ByteArray
        try{
            val md = MessageDigest.getInstance("SHA-256")
            md.update(input.toByteArray())
            hash = md.digest()
        }catch (e: CloneNotSupportedException){
            throw DigestException("couldn't make digest of patial content")
        }
        return "SiGn"+ rsaEncrypt(Base64Utils.encode(hash), getPrivateKey())
    }

    fun verifSign(input: String): Boolean{
        var arr = input.split("SiGn")
        val cipherText = arr[0]
        val sign = rsaDecrypt(arr[1], getPublicKey())

        val hash: ByteArray
        try{
            val md = MessageDigest.getInstance("SHA-256")
            md.update(cipherText.toByteArray())
            hash = md.digest()
        }catch (e: CloneNotSupportedException){
            throw DigestException("couldn't make digest of patial content")
        }
        var hSign = Base64Utils.encode(hash)
        Log.e("싸인3", sign)
        Log.e("싸인4", hSign)
        return hSign == sign
    }

    fun getCertificate(response: String){
        var tempFile = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "certificate.crt")
        try{
            val writer = FileWriter(tempFile)
            val buffer = BufferedWriter(writer)
            buffer.write(response)
            buffer.close()
        }catch(e: java.lang.Exception){
            e.printStackTrace()
        }

        var cf = CertificateFactory.getInstance("X.509")
        var caIn = BufferedInputStream(FileInputStream(tempFile))
        var ca = caIn.use{
            cf.generateCertificate(it) as X509Certificate
        }
        var kf = KeyFactory.getInstance("RSA")
        var public = kf.generatePublic(X509EncodedKeySpec(ca.publicKey.encoded))
        App.prefs.publicKey = Base64Utils.encode(public.encoded)
    }

    fun rsaEncrypt(input: String, key: PrivateKey): String{
        try {
            val cipher = Cipher.getInstance("RSA")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encrypt = cipher.doFinal(input.toByteArray())
            return Base64Utils.encode(encrypt)
        }catch (e: Exception){
            throw RuntimeException(e)
        }
    }

    fun rsaDecrypt(input: String, key: PublicKey): String{
        try {
            var byteEncrypt: ByteArray = Base64Utils.decode(input)
            val cipher = Cipher.getInstance("RSA")
            cipher.init(Cipher.DECRYPT_MODE, key)
            val decrypt = cipher.doFinal(byteEncrypt)
            return String(decrypt)
        }catch (e: Exception){
            throw RuntimeException(e)
        }
    }

    fun getPublicKey(): PublicKey{
        var kf = KeyFactory.getInstance("RSA")
        var public = kf.generatePublic(X509EncodedKeySpec(Base64Utils.decode(App.prefs.publicKey)))
        return public
    }

    fun getPrivateKey(): PrivateKey{
        var kf = KeyFactory.getInstance("RSA")
        var private = kf.generatePrivate(PKCS8EncodedKeySpec(Base64Utils.decode(App.prefs.privateKey)))
        return private
    }
}