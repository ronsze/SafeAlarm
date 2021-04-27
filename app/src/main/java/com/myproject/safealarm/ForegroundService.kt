package com.myproject.safealarm

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
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
import java.time.Duration
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class ForegroundService : Service() {
    private val role = App.prefs.role
    private val lat_1km: Double = 1.0 / 110.9875
    private val lng_1km: Double = 1.0 / 88.3435
    private val lat_1m: Double = lat_1km / 1000.0
    private val lng_1m: Double = lng_1km / 1000.0

    private lateinit var socketT: socketThread
    private var locationManager: LocationManager? = null
    private var locationCount = 0
    private var lat_save: Double = 0.0
    private var lng_save: Double = 0.0
    private var isRight = true
    private var isUp = true
    private var prev_fix = false
    private var log_str = ""

    companion object {
        val mSocket = IO.socket(MyAddress.url)
        const val NOTIFICATION_ID = 20
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START_FOREGROUND -> {
                Log.e("Foreground서비스", "시작 인텐트 받음")
                startForegroundService()
            }
            Actions.STOP_FOREGROUND -> {
                Log.e("Foreground서비스", "종료 인텐트 받음")
                stopForegroundService()
            }
            Actions.HELP_CALL_WARD -> {
                Log.e("Foreground서비스", "피보호자 도움 요청 인텐트 받음")
                mSocket.emit("HelpCall_W")
            }
        }
        if (!mSocket.connected()) {
            connectSocket()
        }
        if (App.prefs.role == "Guard") {

        }
        if (App.prefs.role == "Ward") {
            try {
                locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
                val hasFinePer = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                val hasCoarsePer = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                if (hasFinePer == PackageManager.PERMISSION_GRANTED &&
                    hasCoarsePer == PackageManager.PERMISSION_GRANTED) {
                    locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.0f, gpsListener)
                    locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0.0f, networkListener)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return START_STICKY
    }

    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ통신 관련ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun connectSocket() {
        socketT = socketThread()
        socketT.run()
    }

    inner class socketThread : Thread() {
        override fun run() {
            try {
                mSocket.connect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mSocket.on(Socket.EVENT_CONNECT, onConnectSocket)
            mSocket.on(Socket.EVENT_DISCONNECT, onDiscconectSocket)
            mSocket.on("destDisconnect", onDestDisconnect)
            mSocket.on("requestLoc", onRequestLoc)
            mSocket.on("callbackLoc", onCallbackLoc)
            mSocket.on("HelpCall_W", onHelpCall_W)
            mSocket.on("inOfRange", onInOfRange)
            mSocket.on("outOfRange", onOutOfRange)
            mSocket.on("setGeofence", onSetGeofence)
            mSocket.on("delGeofence", onDelGeofence)
            mSocket.on("sendCerti", onSendCerti)
            if (role == "Guard") {
                sendLocReq()
            }
        }
    }

    private val onConnectSocket = Emitter.Listener {            //최초 연걸
        mSocket.emit("enterRoom", App.prefs.room)
    }
    private val onDiscconectSocket = Emitter.Listener {         //연결 해제
        connectSocket()
    }
    private val onDestDisconnect = Emitter.Listener {           //상대방 연결 끊김
        if (role == "Guard") {
            disconnectAlarm()
        }
    }
    private val onRequestLoc = Emitter.Listener {               //좌표 요청 받음
        if (role == "Ward") {
            getLatLng()
        }
    }

    private val onHelpCall_W = Emitter.Listener {
        if (role == "Guard") {
            receiveHelpCall()
        }
    }

    private val onOutOfRange = Emitter.Listener {               //지정 구역 이탈
        if(role == "Guard"){
            outOfRangeAlarm()
        }
    }

    private val onInOfRange = Emitter.Listener {               //지정 구역 들어옴
        if(role == "Guard"){
            inOfRangeAlarm()
        }
    }

    private val onSetGeofence = Emitter.Listener {              //Geofence설정
        setGeofence()
    }

    private val onDelGeofence = Emitter.Listener {              //Geofence삭제
        delGeofence()
    }

    private val onSendCerti = Emitter.Listener {                //인증 메세지 전송

    }

    private val onCallbackLoc = Emitter.Listener {              //좌표 받음
        if (role == "Guard") {
            var location = it[0].toString()
            try {
                val `object` = JSONObject(location)
                val latitude = `object`.getString("latitude").toDouble()
                val longitude = `object`.getString("longitude").toDouble()
                val fix_msg = `object`.getString("fixed")
                if(fix_msg != "보정없음"){
                    prev_fix = true
                }
                if(prev_fix){
                    if(fix_msg == "보정없음"){
                        log_str = log_str + fix_msg + "\n"
                    }
                }
                if (latitude == 0.0 && longitude == 0.0) {
                    locationCount += 1
                } else {
                    locationCount = 0
                    cngMapLocation(latitude, longitude)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ위치 관련ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun getLatLng() {                                   //피보호자 좌표 구하는 함수
        var latitude = 0.0
        var longitude = 0.0
        var location: Location? = null
        var provider_str: String? = null
        val isGPSEnable = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnable = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        var fix = "보정없음"

        var json = JSONObject()

        if (!isGPSEnable && !isNetworkEnable) {
            provider_str = provider_str.plus("error1")
        } else {
            val hasFinePer = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            val hasCoarsePer = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            if (hasFinePer == PackageManager.PERMISSION_GRANTED &&
                hasCoarsePer == PackageManager.PERMISSION_GRANTED) {
                val loc_g: Location? = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val loc_n: Location? = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (locationManager != null) {
                    if (loc_g != null && loc_n != null) {
                        if (loc_g.accuracy <= loc_n.accuracy) {
                            location = loc_g
                            provider_str = "GPS1"
                        } else {
                            location = loc_n
                            provider_str = "Network1"
                        }
                    } else if (loc_g != null) {
                        location = loc_g
                        provider_str = "GPS2"
                    } else if (loc_n != null) {
                        location = loc_n
                        provider_str = "Network2"
                    }
                    if (location != null) {
                        if((lat_save == 0.0 && lng_save == 0.0) || location.speed <= 0.0f){
                            latitude = location.latitude
                            longitude = location.longitude
                            lat_save = latitude
                            lng_save = longitude
                        }
                        else{
                            var triple: Triple<Double, Double, String> = GPS_Filter(location.latitude, location.longitude, location.speed)
                            latitude = triple.first
                            longitude = triple.second
                            fix = triple.third
                            lat_save = latitude
                            lng_save = longitude
                            isUp = latitude >= lat_save
                            isRight = longitude >= lng_save
                        }
                        json.put("speed", location.speed)
                    } else {
                        provider_str = provider_str.plus("error2")
                    }
                } else {
                    provider_str = provider_str.plus("error3")
                }
            } else {
            }
        }
        try {
            json.put("latitude", latitude)
            json.put("longitude", longitude)
            json.put("provider", provider_str)
            json.put("fixed", fix)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mSocket.emit("callbackLoc", json)
    }

    private fun GPS_Filter(first: Double, second: Double, third: Float): Triple<Double, Double, String> {
        var lat: Double = first
        var lng: Double = second
        var lat_F: Double = first
        var lat_S: Double = first
        var lat_T: Double = first
        var lng_F: Double = second
        var lng_S: Double = second
        var lng_T: Double = second
        val speed: Double = third * 1.3
        var fix: String = "보정없음"
        var isFix = false

        val sub_lat = lat - lat_save
        val sub_lng = lng - lng_save
        val distance = sqrt(sub_lat.pow(2) + sub_lng.pow(2))
        val sum = abs(sub_lat) + abs(sub_lng)
        val ratio_lat = sub_lat/sum
        val ratio_lng = sub_lng/sum
        val ratio_1m = (ratio_lat * lat_1m) + (ratio_lng * lng_1m)
        val ratio_dis = (speed*ratio_1m)/distance

        if(distance >= speed * ratio_1m){
            if(sub_lat >= 0){
                lat_F = lat_save + sub_lat * ratio_dis
            }else{
                lat_F = lat_save - sub_lat * ratio_dis
            }
            if(sub_lng >= 0){
                lng_F = lng_save + sub_lng * ratio_dis
            }else{
                lng_F = lng_save - sub_lng * ratio_dis
            }

            if(isUp){
                lat_S = lat_save + sub_lat * ratio_dis
            }else{
                lat_S = lat_save - sub_lat * ratio_dis
            }
            if(isRight){
                lng_S = lng_save + sub_lng * ratio_dis
            }else{
                lng_S = lng_save - sub_lng * ratio_dis
            }
            isFix = true
        }

        if(isFix){
            if(lat_F > lat_S){
                lat_T = lat_S + ((lat_F - lat_S) / 2.0)
            }else if(lat_F < lat_S){
                lat_T = lat_F + ((lat_S - lat_F) / 2.0)
            }else{
                lat_T = lat_F
            }
            if(lng_F > lng_S){
                lng_T = lng_S + ((lng_F - lng_S) / 2.0)
            }else if(lng_F < lng_S){
                lng_T = lng_F + ((lng_S - lng_F) / 2.0)
            }else{
                lng_T = lng_F
            }
        }

        if(isFix){
            fix = "보정 : Fisrt = ${lat_F}, ${lng_F}/Second = ${lat_S}, ${lng_S}/Third = ${lat_T}, ${lng_T}"
        }
        return Triple(lat, lng, fix)
    }

    private fun cngMapLocation(latitude: Double, longitude: Double) {       //위치 변경 브로드캐스트
        var intent = Intent(App.CNG_LOC)
        intent.putExtra("latitude", latitude)
        intent.putExtra("longitude", longitude)
        App.prefs.s_lat = latitude.toString()
        App.prefs.s_lng = longitude.toString()
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendLocReq() {                                              //위치 요청
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(5000)
                if (locationCount >= 5) {
                    //일정 횟수 이상 응답이 없을 경우 사용자에게 알림
                    Log.e("위치 요청", "응답 없음 5회")
                    faildReceiveLocAlram()
                    locationCount = 0
                }else{
                    mSocket.emit("requestLoc")
                    locationCount += 1
                }
            }
        }
    }

    private fun setGeofence(){                      //Geofence설정

    }

    private fun delGeofence(){                      //Geofence삭제

    }

    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ알람 관련ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun disconnectAlarm() {
        vibratorAlarm()
        val notification = NotificationFile.createNotification(this, "피보호자와 연결이 끊어졌습니다.")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun outOfRangeAlarm() {
        vibratorAlarm()
        val notification = NotificationFile.createNotification(this, "피보호자가 지정 범위를 이탈했습니다.")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun inOfRangeAlarm() {
        vibratorAlarm()
        val notification = NotificationFile.createNotification(this, "피보호자가 지정 범위로 들어왔습니다.")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun faildReceiveLocAlram() {
        vibratorAlarm()
        val notification = NotificationFile.createNotification(this, "피보호자의 위치를 받아오지 못했습니다.")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun receiveHelpCall() {
        vibratorAlarm()
        val notification = NotificationFile.createNotification(this, "피보호자로부터 도움요청을 받았습니다.")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun vibratorAlarm() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect = VibrationEffect.createOneShot(2000, 100)
        vibrator.vibrate(vibrationEffect)
    }

    private fun setAlarm(){                 //시간 알람 설정

    }

    private fun delAlarm(){                 //시간 알람 삭제
        
    }
    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ서비스 관련ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun startForegroundService() {
        val notification = NotificationFile.createNotification(this, " ")
        startForeground(NOTIFICATION_ID, notification)
    }
    private fun stopForegroundService() {
        stopForeground(true)
        stopSelf()
    }
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ리스너ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    val gpsListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            var latitude = location.latitude
            var longitude = location.longitude
            var accuracy = location.accuracy
            var speed = location.speed
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    val networkListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            var latitude = location.latitude
            var longitude = location.longitude
            var accuracy = location.accuracy
            var speed = location.speed
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    inner class alarmManagerReceiver: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            TODO("Not yet implemented")
        }
    }

    override fun onDestroy() {
        Log.e("보정", log_str)
        super.onDestroy()
    }
}
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

object Actions{
    private const val prefix = "com.myproject.safealarm.action"
    const val MAIN = prefix + "main"
    const val START_FOREGROUND = prefix + "startforeground"
    const val STOP_FOREGROUND = prefix + "stopforeground"
    const val HELP_CALL_WARD = prefix + "helpcallWard"
}