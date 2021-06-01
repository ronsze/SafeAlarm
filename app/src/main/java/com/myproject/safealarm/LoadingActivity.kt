package com.myproject.safealarm

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.Spinner
import java.util.Random
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.util.Base64Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.KeyPairGenerator
import java.security.SecureRandom
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import org.bouncycastle.util.io.pem.PemObject
import java.io.*
import java.security.KeyFactory
import java.security.KeyPair
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.X509EncodedKeySpec
import kotlin.math.sign

class LoadingActivity : AppCompatActivity() {
    private val context = this
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private lateinit var loadingDlog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        loadingDlog = LoadingDialog(this)
        loadingDlog.show()
        App.connectSocket()
        getCACertificate()
        checkPermission()
    }

    private fun getCACertificate(){
        var certificate = "-----BEGIN CERTIFICATE-----\n" +
                "MIIDKTCCAhGgAwIBAgIBATANBgkqhkiG9w0BAQsFADA2MQswCQYDVQQGEwJrcjET\n" +
                "MBEGA1UECgwKU3V3b25Vbml2LjESMBAGA1UEAwwJcm9vdGNhWVNZMB4XDTIxMDUy\n" +
                "MjEyNDEwMVoXDTMxMDUyMDEyNDEwMVowNjELMAkGA1UEBhMCa3IxEzARBgNVBAoM\n" +
                "ClN1d29uVW5pdi4xEjAQBgNVBAMMCXJvb3RjYVlTWTCCASIwDQYJKoZIhvcNAQEB\n" +
                "BQADggEPADCCAQoCggEBALxlF0OhUyPiQ2iDxK510XQqktqWoNbdS8vHu0B4ZXlI\n" +
                "v3O25S0MfA7TKQh0FT9F2qwvoP5VHK3MZP/L0aBk2O2PPlS7WUs59tuCE3aP8zci\n" +
                "IKUCOU4kKQdZ2TTNWvn1Sh4+8peSa4mFY0yPnNzL1JRwfRscxl0GXPZ2rGPGx7oS\n" +
                "eYWwns8kOKHMdXtrMWdoYDybwBK+Qfq5HEhRpSVi6kRZIgsDaSzm2lZblNGa9mSk\n" +
                "fqJylrTAtscvjUwKUJM+uy+viZ0BBRP3IgeZm9wJP4aoh2eGBYQNP8c3KaqDJtZo\n" +
                "wpZIaDdo3kpbjSFENBJAsZTDJGmVfHftymeXTnZidBECAwEAAaNCMEAwEgYDVR0T\n" +
                "AQH/BAgwBgEB/wIBADAdBgNVHQ4EFgQUv2QeVPfLh9E2hEUSAK53bEiBNcYwCwYD\n" +
                "VR0PBAQDAgXgMA0GCSqGSIb3DQEBCwUAA4IBAQA7VIX7RhryXd6/H/kg5jocDLr6\n" +
                "UruJ6VnFHuYG2DUs6nUB1iKV8m6Ebwm0AG6RZ1v6bF8gNn1u9GNf8UL93boxqetY\n" +
                "AR7uKheys9JRql8EdZPcar1IYfId35HQ73pBvpE6FaXTGaZH9tyXiYnzzmVR4b69\n" +
                "JR17VPJYMgfs9VOf04OQKRU67FDYAd6kQFl7HhY9B3j0W+ORtv7Ba9NHqjGHEY0U\n" +
                "8k6s/cHcv/qFleva8IOXUVhgSZzHD2B4i0KulSQxflKq/xL3KSrv34yYomTbLPBu\n" +
                "SWwcvBIrRXado49xx/cxZasoD/KLQmK0Pxr34nP/w22tvWsB6KSAixDxDLRI\n" +
                "-----END CERTIFICATE-----"

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

    private fun checkPermission(){          //권한 체크
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION)
        val hasStorageReadPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        val hasStorageWritePermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
            hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED &&
            hasStorageReadPermission == PackageManager.PERMISSION_GRANTED &&
            hasStorageWritePermission == PackageManager.PERMISSION_GRANTED){
            chooseMode()
        }else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])){
                Toast.makeText(this, "앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, MyAddress.PERMISSIONS_REQUEST_CODE)
            }else{
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, MyAddress.PERMISSIONS_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == MyAddress.PERMISSIONS_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size){
            var checkResult = true
            for(result in grantResults){
                if(result != PackageManager.PERMISSION_GRANTED){
                    checkResult = false;
                    break;
                }
            }
            if(checkResult){
                chooseMode()
            }else{
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[3])){
                    Toast.makeText(this, "권한 설정이 거부되었습니다.\n앱을 사용하시려면 다시 실행해주세요.", Toast.LENGTH_SHORT).show()
                    finish()
                }else{
                    Toast.makeText(this, "권한 설정이 거부되었습니다.\n설정에서 권한을 허용해야 합니다..", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun chooseMode(){
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
    }

    private fun registId(){                 //ID 생성
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
                val intent = Intent(context, RegistActivity::class.java)
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

    private fun getRSAKeyPair(): KeyPair{
        val keygen = KeyPairGenerator.getInstance("RSA")
        keygen.initialize(2048, SecureRandom())
        val keyPair = keygen.genKeyPair()
        App.prefs.privateKey = Base64Utils.encode(keyPair.private.encoded)

        return keyPair
    }

    private fun moveActivity(){             //액티비티 이동
        if(App.prefs.role == "Guard"){
            startActivity(Intent(this, GuardActivity::class.java))
        }else{
            startActivity(Intent(this, WardActivity::class.java))
        }
        finish()
    }

    override fun onDestroy() {
        loadingDlog.dismiss()
        super.onDestroy()
    }
}