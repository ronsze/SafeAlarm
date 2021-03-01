package com.myproject.safealarm

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class ForegroundService : Service() {
    private val mSocket = IO.socket(MyAddress.url)
    private val role = App.prefs.role
    private lateinit var socketT: socketThread
    private var locationManager: LocationManager? = null
    private var locationCount = 0
    private var add = 0.0005

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
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ통신 관련ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun connectSocket(){
        socketT = socketThread()
        socketT.run()
    }
    inner class socketThread: Thread(){
        override fun run(){
            try{
                mSocket.connect()
            }catch(e: Exception){
                e.printStackTrace()
            }
            mSocket.on(Socket.EVENT_CONNECT, onConnectSocket)
            mSocket.on(Socket.EVENT_DISCONNECT, onDiscconectSocket)
            mSocket.on("destDisconnect", onDestDisconnect)
            mSocket.on("requestLoc", onRequestLoc)
            mSocket.on("test", onTest)
            mSocket.on("callbackLoc", onCallbackLoc)

            if(role == "Guard"){
                sendLocReq()
            }
        }
    }

    private fun helpCall_G(){                                   //보호자 도움 요청
        mSocket.emit("HelpCall_G")
    }

    private fun helpCall_W(){                                   //피보호자 도움 요청
        mSocket.emit("HelpCall_W")
    }

    private val onTest = Emitter.Listener {
        if(role == "Guard"){
            Log.d("이벤트","테스트 보호자")
        }else{
            Log.d("이벤트","테스트 피보호자")
        }
    }

    private val onConnectSocket = Emitter.Listener {            //최초 연걸
        Log.d("이벤트","최초 연결")
        mSocket.emit("enterRoom", App.prefs.room)
    }
    private val onDiscconectSocket = Emitter.Listener {         //연결 해제
        Log.d("이벤트","연결 해제")
        connectSocket()
    }
    private val onDestDisconnect = Emitter.Listener {           //상대방 연결 끊김
        Log.d("이벤트","상대방 연결 끊김")
    }
    private val onRequestLoc = Emitter.Listener {               //좌표 요청
        if(role == "Ward"){
            getLatLng()
        }
    }
    private val onCallbackLoc = Emitter.Listener {              //좌표 받음
        locationCount = 0
        if(role == "Guard"){
            var location = it[0].toString()
            try{
                val `object` = JSONObject(location)
                var latitude = `object`.getString("latitude").toDouble()
                var longitude = `object`.getString("longitude").toDouble()
                cngMapLocation(latitude, longitude)
            }catch(e: JSONException){
                e.printStackTrace()
            }
        }
    }
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ위치 관련ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun getLatLng(){                    //좌표 구하는 함수
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        var currentLoc_G: Location? = null
        var currentLoc_N: Location? = null
        var userLocation: Location
        var latitude: Double?
        var longitude: Double?
        var hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
        var hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)

        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
            val locationProvider_G = LocationManager.GPS_PROVIDER
            val locationProvider_N = LocationManager.NETWORK_PROVIDER
            currentLoc_G = locationManager?.getLastKnownLocation(locationProvider_G)
            currentLoc_N = locationManager?.getLastKnownLocation(locationProvider_N)
            if(currentLoc_G == null && currentLoc_N == null){
                latitude = 0.0
                longitude = 0.0
            }else{
                if(currentLoc_G == null){
                    userLocation = currentLoc_G!!
                }else if(currentLoc_N == null){
                    userLocation = currentLoc_N!!
                }else{
                    if(currentLoc_G.accuracy > currentLoc_N.accuracy){
                        userLocation = currentLoc_G!!
                    }else{
                       userLocation = currentLoc_N!!
                    }
                }
                latitude = userLocation.latitude
                longitude = userLocation.longitude
            }
            var json = JSONObject()
            try{
                json.put("latitude", latitude + add)
                json.put("longitude", longitude + add)
                add += 0.0005
            }catch(e: JSONException){
                e.printStackTrace()
            }
            mSocket.emit("callbackLoc", json)
        }
    }
    private fun cngMapLocation(latitude: Double, longitude: Double){
        var intent = Intent(App.CNG_LOC)
        intent.putExtra("latitude", latitude)
        intent.putExtra("longitude", longitude)
        App.prefs.s_lat = latitude.toString()
        App.prefs.s_lng = longitude.toString()
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendLocReq(){
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
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ서비스 관련ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
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