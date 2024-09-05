package com.myproject.safealarm.feature.splash

import android.content.Intent
import android.os.Environment
import android.util.Log
import com.myproject.safealarm.App
import com.myproject.safealarm.ResponseDC
import com.myproject.safealarm.Singleton
import com.myproject.safealarm.base.BaseViewModel
import com.myproject.safealarm.feature.register.RegisterActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedInputStream
import java.io.BufferedWriter
import java.io.StringWriter
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.X509EncodedKeySpec
import java.util.*

class SplashViewModel: BaseViewModel() {
    private val _uiState: MutableStateFlow<SplashUiState> = MutableStateFlow(SplashUiState.Loading)
    val uiState get() = _uiState.asStateFlow()

    fun checkUser() {
        val isReg = App.prefs.regKey
        if(App.prefs.idOn){
            if(isReg){
                moveActivity()
            }else{
                registId()
            }
        }else{
            registId()
        }

        App.connectSocket()

        Singleton.server.getCaCert().enqueue(object: Callback<ResponseDC>{
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                val certificate = response.body()!!.result!!
                saveCACertificate(certificate)
                checkPermission()
            }

            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Log.e("인증서", "CA인증서 받기 실패")
            }

        })
    }

    private fun saveCACertificate(certificate: String){
        var tempFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "caCert.crt")
        try{
            val writer = FileWriter(tempFile)
            val buffer = BufferedWriter(writer)
            buffer.write(certificate)
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

        App.prefs.CAPublic = Base64Utils.encode(public.encoded)
    }

    private fun registId() {                 //ID 생성
        val rID = createRandomID()
        Singleton.server.fConnect(rID).enqueue(object:Callback<ResponseDC>{
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                App.prefs.idOn = true
                App.prefs.id = rID
                registCertificate()
            }
            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                registId()
            }
        })
    }

    private fun createRandomID(): String{
        var rNum = Random().nextInt(100000) + 1
        return rNum.toString()
    }

    private fun registCertificate(){
        val csr = createX509CSR()
        Singleton.server.postCSR(App.prefs.id, csr).enqueue(object:Callback<ResponseDC>{
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                val intent = Intent(context, RegisterActivity::class.java)
                App.prefs.csr = csr
                startActivity(intent)
                finish()

            }
            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Log.e("CSR등록", "실패")
            }
        })
    }

    private fun createX509CSR(): String{
        val keyPair = getRSAKeyPair()

        val sigAlg = "SHA256withRSA"
        val params = "C=kr,O=SuwonUniv,CN=${App.prefs.id}"
        val p10builder: PKCS10CertificationRequestBuilder = JcaPKCS10CertificationRequestBuilder(
            X500Name(params), keyPair.public)
        val csBuilder = JcaContentSignerBuilder(sigAlg)
        csBuilder.setProvider(BouncyCastleProvider())
        val signer = csBuilder.build(keyPair.private)
        var p10 = p10builder.build(signer)

        val pemObject = PemObject("CERTIFICATE REQUEST", p10.encoded)
        var csr = StringWriter()
        var jcaPEMWriter = JcaPEMWriter(csr)
        jcaPEMWriter.writeObject(pemObject)
        jcaPEMWriter.close()
        csr.close()
        return csr.toString()
    }

    private fun getRSAKeyPair(): KeyPair {
        val keygen = KeyPairGenerator.getInstance("RSA")
        keygen.initialize(2048, SecureRandom())
        val keyPair = keygen.genKeyPair()
        App.prefs.privateKey = Base64Utils.encode(keyPair.private.encoded)

        return keyPair
    }

    sealed interface SplashUiState {
        data object Loading: SplashUiState
        data object LoggedIn: SplashUiState
        data object LoggedOut: SplashUiState
    }
}