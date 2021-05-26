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
import io.socket.client.IO
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
        backActivity()
        super.onBackPressed()
    }

    private val onReceivePrime = Emitter.Listener{
        var tmp = it[0].toString().split("9y6s0y9")
        var receiveId = tmp[0]
        Singleton.server.getCert(receiveId).enqueue(object:Callback<ResponseDC>{
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                getCertificate(response.body()!!.result!!)
                sendR1(tmp[1])
            }
            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Log.e("인증서", "실패")
            }
        })
    }
    private fun sendR1(input: String){
        var msg = input.split("SiGn")
        var primeStr = msg[0]
        if(verifSign(input)){
            var arr = primeStr.split(".")
            this.p = arr[0].toBigInteger()
            this.g = arr[1].toBigInteger()
            var x = BigInteger(1024, Random())
            while(x > p.subtract(1.toBigInteger())){
                x = BigInteger(1024, Random())
            }
            this.x = x
            var r1 = g.modPow(x, p)
            mSocket_R.emit("sendR1", r1.toString()+getSign(r1.toString()))
        }else{
            Log.e("서명 sendR1", "서명 불일치")
        }
    }

    private val onReceiveR2 = Emitter.Listener {
        val msg = it[0].toString().split("SiGn")
        val r2 = msg[0].toBigInteger()
        if(verifSign(it[0].toString())){
            var key = r2.modPow(x, p)
            App.prefs.key = key.toString()
            registGuard()
        }else{
            Log.e("서명 receiveR2", "서명 불일치")
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