package com.myproject.safealarm

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.google.android.gms.common.util.Base64Utils
import com.myproject.safealarm.databinding.ActivityMissingInfoBinding
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import org.bouncycastle.util.io.pem.PemObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.lang.Exception
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class MissingInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMissingInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMissingInfoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val keygen = KeyPairGenerator.getInstance("RSA")
        keygen.initialize(2048, SecureRandom())
        val keyPair = keygen.genKeyPair()

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
        App.prefs.csr = csr.toString()

        binding.refresh.setOnClickListener {
            Singleton.server.postCSR(App.prefs.id, App.prefs.csr).enqueue(object:Callback<ResponseDC>{
                override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                    Log.e("성공", "성공")
                }
                override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                    Log.e("실패", "실패")
                }

            })
        }
        binding.button.setOnClickListener {
            Singleton.server.getCert(App.prefs.id).enqueue(object:Callback<ResponseDC>{
                override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                    Log.e("인증서", response.body()!!.result!!)
                    getCertificate(response.body()!!.result!!)

                }
                override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                    Log.e("인증서", "실패")
                }
            })
        }
    }

    fun getCertificate(response: String){
        var tempFile = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "crt.crt")
        try{
            val writer = FileWriter(tempFile)
            val buffer = BufferedWriter(writer)
            buffer.write(response)
            buffer.close()
        }catch(e: Exception){
            e.printStackTrace()
        }

        var cf = CertificateFactory.getInstance("X.509")
        var caIn = BufferedInputStream(FileInputStream(tempFile))
        var ca = caIn.use{
            cf.generateCertificate(it) as X509Certificate
        }
        Log.e("주체", ca.subjectDN.toString())
        Log.e("공개키", Base64Utils.encode(ca.publicKey.encoded))
    }
}