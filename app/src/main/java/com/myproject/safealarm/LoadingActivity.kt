package com.myproject.safealarm

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import java.io.StringWriter
import java.security.KeyPair
import kotlin.math.sign

class LoadingActivity : AppCompatActivity() {
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private lateinit var loadingDlog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        loadingDlog = LoadingDialog(this)
        loadingDlog.show()
        App.connectSocket()
        checkPermission()
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
                chooseMode()
                App.prefs.csr = csr
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