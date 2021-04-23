package com.myproject.safealarm

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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

class ForegroundService : Service() {
    private val role = App.prefs.role
    private val lat_1km: Double = 1.0/110.9875
    private val lng_1km: Double = 1.0/88.3435
    private val lat_1m: Double = lat_1km/1000.0
    private val lng_1m: Double = lng_1km/1000.0

    private lateinit var socketT: socketThread
    private var locationManager: LocationManager? = null
    private var locationCount = 0
    private var isOutOfRange = false
    private var lat_save: Double = 0.0
    private var lng_save: Double = 0.0

    companion object{
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
        if(App.prefs.role == "Guard"){
            registTimeSet()
        }
        if(App.prefs.role == "Ward"){
            try{
                locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
                val hasFinePer = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                val hasCoarsePer = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                if (hasFinePer == PackageManager.PERMISSION_GRANTED &&
                    hasCoarsePer == PackageManager.PERMISSION_GRANTED) {
                    locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.0f, gpsListener)
                    locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0.0f, networkListener)
                }
            }catch(e: Exception){
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
            mSocket.on("outOfRange", onOutOfRange)
            mSocket.on("HelpCall_W", onHelpCall_W)
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
        if(role == "Guard"){
            disconnectAlarm()
        }
    }
    private val onRequestLoc = Emitter.Listener {               //좌표 요청
        if (role == "Ward") {
            getLatLng()
        }
    }

    private val onOutOfRange = Emitter.Listener {               //지정 범위 이탈
        if(role == "Guard") {
            OutOfRangeAlarm()
        }
    }

    private val onHelpCall_W = Emitter.Listener {
        if(role == "Guard"){
            receiveHelpCall()
        }
    }
    private val onCallbackLoc = Emitter.Listener {              //좌표 받음
        if (role == "Guard") {
            var location = it[0].toString()
            try {
                val `object` = JSONObject(location)
                val latitude = `object`.getString("latitude").toDouble()
                val longitude = `object`.getString("longitude").toDouble()
                if(latitude == 0.0 && longitude == 0.0){
                    locationCount += 1
                }else{
                    locationCount = 0
                    cngMapLocation(latitude, longitude)
                    checkRange(latitude, longitude)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ위치 관련ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun getLatLng() {                                   //피보호자 좌표 구하는 함수
        var latitude = 0.0
        var longitude = 0.0
        var accuracy1 = 0.0f
        var accuracy2 = 0.0f
        var speed = 0.0f
        var location: Location? = null
        var provider_str: String? = null
        var Pair: Pair<Double, Double>
        val isGPSEnable = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnable = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        var json = JSONObject()

        if(!isGPSEnable && !isNetworkEnable){
            provider_str = provider_str.plus("error1")
        }else{
            val hasFinePer = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            val hasCoarsePer = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            if (hasFinePer == PackageManager.PERMISSION_GRANTED &&
                hasCoarsePer == PackageManager.PERMISSION_GRANTED) {
                val loc_g: Location? = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val loc_n: Location? = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if(locationManager != null){
                    if(loc_g != null && loc_n != null){
                        if(loc_g.accuracy <= loc_n.accuracy){
                            accuracy1 = loc_g.accuracy
                            accuracy2 = loc_n.accuracy
                            location = loc_g
                            provider_str = "GPS1"
                        }else{
                            accuracy1 = loc_g.accuracy
                            accuracy2 = loc_n.accuracy
                            location = loc_n
                            provider_str = "Network1"
                        }
                    }else if(loc_g != null){
                        location = loc_g
                        provider_str = "GPS2"
                    }else if(loc_n != null){
                        location = loc_n
                        provider_str = "Network2"
                    }
                    if(location != null){
                        accuracy1 = location?.accuracy
                        speed = location?.speed
                        Pair = GPS_Filter(location?.latitude, location?.longitude, speed)
                        latitude = Pair.first
                        longitude = Pair.second
                        lat_save = latitude
                        lng_save = longitude

                    }else{
                        provider_str = provider_str.plus("error2")
                    }
                }else{
                    provider_str = provider_str.plus("error3")
                }
            }else{
            }
        }
        try {
            json.put("latitude", latitude)
            json.put("longitude", longitude)
            json.put("provider", provider_str)
            json.put("accuracy1", accuracy1)
            json.put("accuracy2", accuracy2)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mSocket.emit("callbackLoc", json)
    }

    private fun GPS_Filter(first: Double, second: Double, speed: Float): Pair<Double, Double>{
        var lat = first
        var lng = second
        val lat_speed = speed*lat_1m*5*1.1
        val lng_speed = speed*lng_1m*5*1.1
        val sub_lat = lat - lat_save
        val sub_lng = lng - lng_save
        if(Math.abs(sub_lat) > lat_speed){
            if(sub_lat >= 0){
                lat = lat_save + lat_speed
            }else{
                lat = lat_save - lat_speed
            }
            Log.e("좌표 보정", "${first} -> ${lat}")
        }
        if(Math.abs(sub_lng) > lng_speed){
            if(sub_lng >= 0){
                lng = lng_save + lng_speed
            }else{
                lng = lng_save - lng_speed
            }
            Log.e("좌표 보정", "${second} -> ${lng}")
        }
        return Pair(lat, lng)
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
                mSocket.emit("requestLoc")
                if (locationCount >= 5) {
                    //일정 횟수 이상 응답이 없을 경우 사용자에게 알림
                    Log.e("위치 요청", "응답 없음 5회")
                    faildReceiveLocAlram()
                    locationCount = 0
                }
                locationCount += 1
            }
        }
    }
    private fun checkRange(lat: Double, lng: Double){                       //범위 이탈 확인
        if(lat < (App.prefs.center_lat.toDouble()-(lat_1km*App.prefs.range_km))
            || lng < (App.prefs.center_lng.toDouble()-(lng_1km*App.prefs.range_km))
            || lat > (App.prefs.center_lat.toDouble()+(lat_1km*App.prefs.range_km))
            || lng > (App.prefs.center_lng.toDouble()+(lng_1km*App.prefs.range_km))){
            if(!isOutOfRange){
                OutOfRangeAlarm()
                isOutOfRange = true
            }
        }else{
            if(isOutOfRange){
                inOfRangeAlarm()
            }
            isOutOfRange = false
        }
    }
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ알람 관련ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun disconnectAlarm(){
        vibratorAlarm()
        val notification = NotificationFile.createNotification(this, "피보호자와 연결이 끊어졌습니다.")
        startForeground(NOTIFICATION_ID, notification)
    }
    private fun inOfRangeAlarm(){
        vibratorAlarm()
        val notification = NotificationFile.createNotification(this, "피보호자가 지정 범위로 들어왔습니다.")
        startForeground(NOTIFICATION_ID, notification)
    }
    private fun OutOfRangeAlarm(){
        vibratorAlarm()
        val notification = NotificationFile.createNotification(this, "피보호자가 지정 범위를 이탈했습니다.")
        startForeground(NOTIFICATION_ID, notification)
    }
    private fun faildReceiveLocAlram(){
        vibratorAlarm()
        val notification = NotificationFile.createNotification(this, "피보호자의 위치를 받아오지 못했습니다.")
        startForeground(NOTIFICATION_ID, notification)
    }
    private fun receiveHelpCall(){
        vibratorAlarm()
        val notification = NotificationFile.createNotification(this, "피보호자로부터 도움요청을 받았습니다.")
        startForeground(NOTIFICATION_ID, notification)
    }
    private fun vibratorAlarm(){
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect = VibrationEffect.createOneShot(2000, 100)
        vibrator.vibrate(vibrationEffect)
    }

    private fun registTimeSet(){
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, 2021)
        calendar.set(Calendar.MONTH, 2)         //0부터 시작 = 월에서 -1
        calendar.set(Calendar.DATE, 29)
        calendar.set(Calendar.HOUR_OF_DAY, 23)  //한국 시간에서 -9시간, 맞춰서 DATE도 바꿔야됨
        calendar.set(Calendar.MINUTE, 2)
        calendar.set(Calendar.SECOND, 50)       //Foreground에서 사용 시 10초정도 딜레이 있음

        val intent = Intent(this, rangeBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ서비스 관련ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun startForegroundService(){
        val notification = NotificationFile.createNotification(this, " ")
        startForeground(NOTIFICATION_ID, notification)
    }
    private fun stopForegroundService(){
        stopForeground(true)
        stopSelf()
    }
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ위치 리스너ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    val gpsListener = object: LocationListener{
        override fun onLocationChanged(location: Location) {
            var latitude = location.latitude
            var longitude = location.longitude
            var accuracy = location.accuracy
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }
        override fun onProviderEnabled(provider: String) {
        }
        override fun onProviderDisabled(provider: String) {
        }
    }

    val networkListener = object: LocationListener {
        override fun onLocationChanged(location: Location) {
            var latitude = location.latitude
            var longitude = location.longitude
            var accuracy = location.accuracy
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }
        override fun onProviderEnabled(provider: String) {
        }
        override fun onProviderDisabled(provider: String) {
        }
    }
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
}

class rangeBroadcastReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("알람테스트", "확인")
        Log.d("알람테스트", ForegroundService.mSocket.connect().toString())
    }
}

object Actions{
    private const val prefix = "com.myproject.safealarm.action"
    const val MAIN = prefix + "main"
    const val START_FOREGROUND = prefix + "startforeground"
    const val STOP_FOREGROUND = prefix + "stopforeground"
    const val HELP_CALL_WARD = prefix + "helpcallWard"
}