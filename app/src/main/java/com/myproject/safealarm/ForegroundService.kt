package com.myproject.safealarm

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

class ForegroundService : Service() {
    val mSocket = IO.socket(MyAddress.url)
    val role = App.prefs.role
    lateinit var socketT: socketThread
    var locationManager: LocationManager? = null
    var locationCount = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            Actions.START_FOREGROUND -> {
                Log.e("Foreground서비스", "시작 인텐트 받음")
                startForegroundService()
            }
            Actions.STOP_FOREGROUND -> {
                Log.e("Foreground서비스", "종료 인텐트 받음")
                stopForegroundService()
            }
            Actions.HELP_CALL_WARD ->{
                Log.e("Foreground서비스", "피보호자 도움 요청 인텐트 받음")
                helpCall_W()

            }
            Actions.HELP_CALL_GUARD ->{
                Log.e("Foreground서비스", "보호자 도움 요청 인텐트 받음")
                helpCall_G()
            }
        }
        if(mSocket.connected()){

        }else{
            connectSocket()
        }
        return START_STICKY
    }
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun connectSocket(){
        socketT = socketThread()
        socketT.run()
    }
    inner class socketThread: Thread(){
        override fun run(){
            try{
                mSocket.connect()
                Log.e("소켓 생성", "성공")
            }catch(e: Exception){
                Log.e("소켓 생성", "실패")
                Log.e("소켓 오류", e.toString())
            }
            mSocket.on(Socket.EVENT_CONNECT, onConnectSocket)
            mSocket.on(Socket.EVENT_DISCONNECT, onDiscconectSocket)
            mSocket.on("destDisconnect", onDestDisconnect)
            mSocket.on("requestLoc", onRequestLoc)
            mSocket.on("test", onTest)
            mSocket.on("callbackLoc", onCallbackLoc)
            if(role == "Guard"){
                CoroutineScope(Dispatchers.IO).launch {
                    while(true){
                        delay(5000)
                        mSocket.emit("requestLoc")
                        if(locationCount >= 3){
                            //일정 횟수 이상 응답이 없을 경우 사용자에게 알림
                            Log.e("위치 요청", "응답 없음 3회")
                        }
                        locationCount += 1
                    }
                }
            }
        }
    }

    fun helpCall_W(){
        mSocket.emit("HelpCall_W")
    }

    fun helpCall_G(){
        mSocket.emit("HelpCall_G")
    }

    val onTest = Emitter.Listener {
        if(role == "Guard"){
            Log.d("이벤트","테스트 보호자")
        }else{
            Log.d("이벤트","테스트 피보호자")
        }
    }

    val onConnectSocket = Emitter.Listener {
        //최초 연걸
        Log.d("이벤트","최초 연결")
        mSocket.emit("enterRoom", App.prefs.room)
    }
    val onDiscconectSocket = Emitter.Listener {
        //연결 해제
        Log.d("이벤트","연결 해제")
        connectSocket()
    }
    val onDestDisconnect = Emitter.Listener {
        //상대방 연결 끊김
        Log.d("이벤트","상대방 연결 끊김")
    }
    val onRequestLoc = Emitter.Listener {
        //좌표 요청
        Log.d("이벤트","좌표 요청 받음")
        if(role == "Ward"){
            getLatLng()
        }
    }
    val onCallbackLoc = Emitter.Listener { 
        //좌표 받음
        locationCount = 0
        if(role == "Guard"){
            Log.e("좌표 받음", "받음")
            var location = it[0].toString()
            try{
                val `object` = JSONObject(location)
                var latitude = `object`.getString("latitude")
                var longitude = `object`.getString("longitude")
                Log.d("좌표 받음", "위도 : ${latitude}, 경도 : ${longitude}")
            }catch(e: JSONException){
                Log.e("좌표 받음", "에러 ${e}")
            }
        }
    }
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

    private fun getLatLng(){      //좌표 구하는 함수
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        var currentLoc: Location? = null
        var latitude: Double?
        var longitude: Double?
        var userLocation: Location
        var hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
        var hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
            val locationProvider_G = LocationManager.GPS_PROVIDER
            val locationProvider_N = LocationManager.NETWORK_PROVIDER
            Log.d("좌표 함수", "함수실행")
            currentLoc = locationManager?.getLastKnownLocation(locationProvider_G)
            if(currentLoc == null){
                currentLoc = locationManager?.getLastKnownLocation(locationProvider_N)
                if(currentLoc == null){
                    Log.d("좌표 함수", "함수실행")
                    latitude = 0.0
                    longitude = 0.0
                }else{
                    Log.d("좌표 함수", "함수실행")
                    userLocation = currentLoc
                    latitude = userLocation.latitude
                    longitude = userLocation.longitude
                }
            }else{
                userLocation = currentLoc
                latitude = userLocation.latitude
                longitude = userLocation.longitude
            }
            var json = JSONObject()
            try{
                Log.d("좌표 전송", "위도 : ${latitude}, 경도 : ${longitude}")
                json.put("latitude", latitude)
                json.put("longitude", longitude)
            }catch(e: JSONException){
                e.printStackTrace()
            }
            mSocket.emit("callbackLoc", json)
        }
    }

    val mLocationListener = object: LocationListener{
        override fun onLocationChanged(location: Location) {
            var longitude: Double?
            var latitude: Double?
            var json = JSONObject()
            if(location.provider == LocationManager.GPS_PROVIDER){
                longitude = location.longitude
                latitude = location.latitude
                Log.d("CheckCurrentLocation", "GPS현재 내 위치 값: ${latitude}, ${longitude}")
            }else{
                longitude = location.longitude
                latitude = location.latitude
                Log.d("CheckCurrentLocation", "NETWORK현재 내 위치 값: ${latitude}, ${longitude}")
            }
            try{
                json.put("latitude", latitude)
                json.put("longitude", longitude)
            }catch(e: JSONException){
                e.printStackTrace()
            }
            mSocket.emit("callbackLoc", json)
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            super.onStatusChanged(provider, status, extras)
        }
        override fun onProviderEnabled(provider: String) {
            super.onProviderEnabled(provider)
        }
        override fun onProviderDisabled(provider: String) {
            super.onProviderDisabled(provider)
        }
    }
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun startForegroundService(){
        val notification = NotificationFile.createNotification(this)
        startForeground(NOTIFICATION_ID, notification)
    }
    private fun stopForegroundService(){
        stopForeground(true)
        stopSelf()
    }

    companion object{
        const val NOTIFICATION_ID = 20
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}
object Actions{
    private const val prefix = "com.myproject.safealarm.action"
    const val MAIN = prefix + "main"
    const val START_FOREGROUND = prefix + "startforeground"
    const val STOP_FOREGROUND = prefix + "stopforeground"
    const val HELP_CALL_WARD = prefix + "helpcallWard"
    const val HELP_CALL_GUARD = prefix + "helpcallGuard"
}