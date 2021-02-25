package com.myproject.safealarm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.Random
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoadingActivity : AppCompatActivity() {
    val context = this
    var REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        Log.d("역할", App.prefs.role)
        checkPermission()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == MyAddress.PERMISSIONS_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size){
            var check_result = true
            for(result in grantResults){
                if(result != PackageManager.PERMISSION_GRANTED){
                    check_result = false;
                    break;
                }
            }
            if(check_result){
                readPref()
            }else{
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])){
                    Toast.makeText(this, "권한 설정이 거부되었습니다.\n앱을 사용하시려면 다시 실행해주세요.", Toast.LENGTH_SHORT).show()
                    finish()
                }else{
                    Toast.makeText(this, "권한 설정이 거부되었습니다.\n설정에서 권한을 허용해야 합니다..", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun checkPermission(){
        var hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
        var hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)

        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
            readPref()
        }else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])){
                Toast.makeText(this, "앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, MyAddress.PERMISSIONS_REQUEST_CODE)
            }else{
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, MyAddress.PERMISSIONS_REQUEST_CODE)
            }
        }
    }

    private fun readPref(){
        Log.d("id생성", "${App.prefs.idOn}, ${App.prefs.id}")
        val isReg = App.prefs.regKey
        Log.d("역할", App.prefs.role)
        if(App.prefs.idOn){
            if(isReg){
                moveActivity()
            }else{
                createId()
            }
        }else{
            createId()
        }
    }

    private fun createId(){
        var rNum = Random().nextInt(100000) + 1
        Singleton.server.fConnect(rNum.toString()).enqueue(object:Callback<ResponseDC>{
            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
                Log.d("에러", t.toString())
                createId()
            }
            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
                Log.d("접속", "${response.body()!!.result!!.toBoolean()}, ${rNum}")
                Log.d("액티비티 이동", "로딩->등록")
                startActivity(Intent(context, RegistActivity::class.java))
                finish()
            }
        })
        App.prefs.idOn = true
        App.prefs.id = rNum.toString()
    }
    private fun moveActivity(){
        Log.d("역할", App.prefs.role)
        if(App.prefs.role == "Guard"){
            Log.d("액티비티 이동", "로딩->보호자")
            startActivity(Intent(this, GuardActivity::class.java))
        }else{
            Log.d("액티비티 이동", "로딩->피보호자")
            startActivity(Intent(this, WardActivity::class.java))
        }
        finish()
    }
}