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
import com.google.android.gms.common.util.Base64Utils
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Duration
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.*

class ForegroundService : Service() {
    private val role = App.prefs.role
    private lateinit var socketT: socketThread
    private var locationManager: LocationManager? = null

    private val lat1km: Double = 1.0 / 110.9875 ; private val lng1km: Double = 1.0 / 88.3435
    private var lat_save: Double = 0.0 ; private var lng_save: Double = 0.0
    private var locationCount = 0
    private var prev_lat = 0.0 ; private var prev_lng = 0.0 ; private var prev_dis = 0.0
    private var arr: FloatArray = floatArrayOf(1.0f)
    private var isFix_S = false
    private var now_cell = 0
    private lateinit var key: ByteArray
    private lateinit var privateKey: PrivateKey
    private var isSendCert = false
    private var isCert = false
    private var first = false

    private var entireData: MutableList<MutableList<Int>> = mutableListOf(
        mutableListOf(56,57,58,59,60,50,49,48,47,46,56),
        mutableListOf(56,57,67,68,69,59,58,48,47,46,56),
        mutableListOf(56,57,67,77,78,79,69,59,58,57,56),
        mutableListOf(56,57,58,59,49,48,47,46,56),
        mutableListOf(56,57,58,68,67,66,56),
        mutableListOf(56,57,58,68,67,57,47,46,56),
        mutableListOf(56,57,58,48,47,57,67,66,56),
        mutableListOf(56,57,58,59,69,68,67,57,47,46,45,55,56),
        mutableListOf(56,46,47,48,58,68,67,57,56),
        mutableListOf(56,66,67,68,69,59,58,48,38,37,47,46,56),
        mutableListOf(56,55,45,46,36,37,38,39,49,59,69,68,67,66,56),
        mutableListOf(56,55,45,46,47,48,58,68,67,66,56),
        mutableListOf(56,55,65,66,67,57,58,68,67,66,56),
        mutableListOf(56,66,65,55,45,55,56),
        mutableListOf(56,66,67,68,69,59,58,48,47,46,45,55,56),
        mutableListOf(56,57,58,59,60,50,49,48,47,46,56),
        mutableListOf(56,57,67,68,69,59,58,48,47,46,56),
        mutableListOf(56,46,47,48,58,68,67,57,56),
        mutableListOf(56,66,67,68,69,59,58,48,38,37,47,46,56),
        mutableListOf(56,55,45,46,36,37,38,39,49,59,69,68,67,66,56),
        mutableListOf(56,55,45,46,47,48,58,68,67,66,56),
        mutableListOf(56,55,65,66,67,57,58,68,67,66,56),
        mutableListOf(56,66,65,55,45,55,56),
        mutableListOf(56,66,67,68,69,59,58,48,47,46,45,55,56),
        mutableListOf(56,57,58,59,60,50,49,48,47,46,56),
        mutableListOf(56,57,67,68,69,59,58,48,47,46,56),
        mutableListOf(56,46,47,48,58,68,67,57,56),
        mutableListOf(56,66,67,68,69,59,58,48,38,37,47,46,56),
        mutableListOf(56,55,45,46,36,37,38,39,49,59,69,68,67,66,56),
        mutableListOf(56,55,45,46,47,48,58,68,67,66,56),
        mutableListOf(56,55,65,66,67,57,58,68,67,66,56),
        mutableListOf(56,66,65,55,45,55,56),
        mutableListOf(56,66,67,68,69,59,58,48,47,46,45,55,56),
        mutableListOf(55,57,58,59,60,50,49,48,47,46,56),
        mutableListOf(55,57,67,68,69,59,58,48,47,46,56),
        mutableListOf(55,46,47,48,58,68,67,57,56),
        mutableListOf(55,66,67,68,69,59,58,48,38,37,47,46,56),
        mutableListOf(55,55,45,46,36,37,38,39,49,59,69,68,67,66,56),
        mutableListOf(55,55,45,46,47,48,58,68,67,66,56),
        mutableListOf(55,55,65,66,67,57,58,68,67,66,56),
        mutableListOf(55,46,45,55,56),
        mutableListOf(55,46,47,48,49,59,58,57,56),
        mutableListOf(55,46,47,48,58,68,67,66,56),
        mutableListOf(55,66,65,55,45,55,56),
        mutableListOf(55,66,67,68,69,59,58,48,47,46,45,55,56),
        mutableListOf(66,57,58,59,60,50,49,48,47,46,56),
        mutableListOf(66,57,67,68,69,59,58,48,47,46,56),
        mutableListOf(66,46,47,48,58,68,67,57,56),
        mutableListOf(66,66,67,68,69,59,58,48,38,37,47,46,56),
        mutableListOf(66,55,45,46,36,37,38,39,49,59,69,68,67,66,56),
        mutableListOf(66,55,45,46,47,48,58,68,67,66,56),
        mutableListOf(66,55,65,66,67,57,58,68,67,66,56),
        mutableListOf(66,46,45,55,56),
        mutableListOf(66,46,47,48,49,59,58,57,56),
        mutableListOf(66,46,47,48,58,68,67,66,56),
        mutableListOf(66,66,65,55,45,55,56),
        mutableListOf(66,66,67,68,69,59,58,48,47,46,45,55,56),
        mutableListOf(57,57,58,59,60,50,49,48,47,46,56),
        mutableListOf(57,57,67,68,69,59,58,48,47,46,56),
        mutableListOf(57,46,47,48,58,68,67,57,56),
        mutableListOf(57,66,67,68,69,59,58,48,38,37,47,46,56),
        mutableListOf(57,55,45,46,36,37,38,39,49,59,69,68,67,66,56),
        mutableListOf(57,55,45,46,47,48,58,68,67,66,56),
        mutableListOf(57,55,65,66,67,57,58,68,67,66,56),
        mutableListOf(57,46,45,55,56),
        mutableListOf(57,46,47,48,49,59,58,57,56),
        mutableListOf(57,46,47,48,58,68,67,66,56),
        mutableListOf(57,66,65,55,45,55,56),
        mutableListOf(57,66,67,68,69,59,58,48,47,46,45,55,56)
    )
    private var tmpData: MutableList<MutableList<Int>> = mutableListOf()

    companion object {
        val mSocket = App.mSocket
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
        key = getKey()
        if (mSocket.connected()) {
            connectSocket()
        }
        if (App.prefs.role == "Guard") {
            tmpData.addAll(entireData)
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
        LocalBroadcastManager.getInstance(this).registerReceiver(alarmManagerReceiver(), IntentFilter("Alarm"))
        LocalBroadcastManager.getInstance(this).registerReceiver(timerReceiver(), IntentFilter("timer"))
        return START_STICKY
    }

    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ통신 관련ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun connectSocket() {
        socketT = socketThread()
        if(socketT.isAlive){

        }else{
            socketT.run()
        }
    }

    inner class socketThread : Thread() {
        override fun run() {
            try {

            } catch (e: Exception) {
                e.printStackTrace()
            }
            mSocket.on(Socket.EVENT_CONNECT, onConnectSocket)
            mSocket.on(Socket.EVENT_DISCONNECT, onDiscconectSocket)
            mSocket.emit("enterRoom", App.prefs.room)
            mSocket.on("destDisconnect", onDestDisconnect)
            mSocket.on("requestLoc", onRequestLoc)
            mSocket.on("callbackLoc", onCallbackLoc)
            mSocket.on("HelpCall_W", onHelpCall_W)
            mSocket.on("sendCert", onSendCert)
            mSocket.on("callbackCert", onCallbackCert)
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
        createNotification("상대방과 연결이 끊어졌습니다.")
    }

    private val onRequestLoc = Emitter.Listener {               //좌표 요청 받음
        if (role == "Ward") {
            try{
                getLatLng()
            }catch (e:Exception){
                e.printStackTrace()
            }

        }
    }

    private val onHelpCall_W = Emitter.Listener {
        if (role == "Guard") {
            createNotification("피보호자로부터 도움 요청을 받았습니다.")
        }
    }

    private val onSendCert = Emitter.Listener {                //인증 메세지 받음(피보호자)
        if(role == "Ward"){
            createNotification("보호자로부터 인증 요청이 도착했습니다.\n3분 안에 인증을 완료해주세요.")
            App.prefs.cert = true
            var timer = TimerThread_W()
            timer.run()
        }
    }

    private val onCallbackCert = Emitter.Listener {            //인증 메세지 완료
        if(role == "Guard"){
            if(isSendCert){
                createNotification("피보호자가 인증을 완료했습니다.")
                isCert = true
            }
        }
    }

    private val onCallbackLoc = Emitter.Listener {              //좌표 받음
        if (role == "Guard") {
            var location = it[0].toString()
            try {
                val `object` = JSONObject(location)
                val deLat = decrypt(`object`.getString("latitude"), key)
                val deLng = decrypt(`object`.getString("longitude"), key)
                if(verifSign(deLat) && verifSign(deLng)){
                    val latitude = deLat.split("SiGn")[0].toDouble()
                    val longitude = deLng.split("SiGn")[0].toDouble()
                    if (latitude == 0.0 && longitude == 0.0) {
                        locationCount += 1
                    } else {
                        locationCount = 0
                        cngMapLocation(latitude, longitude)
                        first = true
                        var cell = getCell(latitude, longitude)
                        if (first) {
                            checkCell(cell)
                        }
                    }
                }else{
                    locationCount += 1
                }
            } catch (e: JSONException) {
                locationCount += 1
                e.printStackTrace()
            }
        }
    }

//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ위치 관련ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun getLatLng() {                                   //피보호자 좌표 구하는 함수
        var lat = 0.0 ; var lng = 0.0
        var location: Location? = null
        var provider_str: String? = null ; var fix = "보정없음"
        val isGPSEnable = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnable = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
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
                    if(loc_g != null && loc_n != null){
                        if (loc_g.accuracy <= loc_n.accuracy) {
                            location = loc_g
                            provider_str = "GPS1"
                        } else {
                            location = loc_n
                            provider_str = "Network1"
                        }
                    }
                    if (location != null) {
                        if((lat_save == 0.0 && lng_save == 0.0)){
                            lat = location.latitude
                            lng = location.longitude
                            lat_save = lat ; lng_save = lng
                        }
                        else{
                            val triple: Triple<Double, Double, String> = GPS_Filter(location)
                            lat = triple.first
                            lng = triple.second
                            fix = triple.third

                            if(lat - lat_save == 0.0 && lng - lng_save == 0.0){ }
                            else if(isFix_S) {
                                prev_dis = 0.0
                            }
                            else {
                                prev_lat = lat - lat_save ; prev_lng = lng - lng_save
                                Location.distanceBetween(lat_save, lng_save, lat, lng, arr)
                                prev_dis = abs(arr[0]).toDouble()
                            }
                            lat_save = lat ; lng_save = lng
                        }
                        json.put("accuracy", location.accuracy)
                        json.put("speed", location.speed)
                    } else {
                        provider_str = provider_str.plus("error2")
                    }
                } else {
                    provider_str = provider_str.plus("error3")
                }
            } else { }
        }
        try {
            var enLat = encrypt(lat.toString(), key)
            var enLng = encrypt(lng.toString(), key)
            json.put("latitude", enLat)
            json.put("longitude", enLng)
            json.put("fixed", fix)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mSocket.emit("callbackLoc", json)
    }

    private fun GPS_Filter(location: Location): Triple<Double, Double, String> {
        var first = location.latitude ; var second = location.longitude ; var third = location.speed
        var lat_F: Double = first ; var lat_S: Double = first ; var lat_T: Double = first
        var lng_F: Double = second ; var lng_S: Double = second ; var lng_T: Double = second
        var fix = "보정없음" ; var isFix = false
        var speed = third * 5.0
        val sub_lat = first - lat_save ; val sub_lng = second - lng_save
        var ratio_dis = 0.0

        var arr: FloatArray = floatArrayOf(1.0f)
        Location.distanceBetween(lat_save, lng_save, first, second, arr)
        val distance = abs(arr[0])
        //val ratio_dis = speed/distance

        if(distance >= speed + 5.0){
            if(speed == 0.0) speed = 5.0
            else speed += 2.0

            ratio_dis = speed/distance
            lat_F = lat_save + sub_lat * ratio_dis
            lng_F = lng_save + sub_lng * ratio_dis

            if(prev_dis == 0.0){
                lat_S = lat_save + sub_lat * ratio_dis
                lng_S = lng_save + sub_lng * ratio_dis
                isFix_S = false
                lat_T = lat_F
                lng_T = lng_F
            }else {
                ratio_dis = prev_dis / speed
                lat_S = lat_save + prev_lat * ratio_dis
                lng_S = lng_save + prev_lng * ratio_dis
                isFix_S = true
                lat_T = (lat_F + lat_S) / 2.0
                lng_T = (lng_F + lng_S) / 2.0
            }

            isFix = true
        }
        if(isFix){
            fix = "보정 : ${first} -> ${lat_T}, ${second} -> ${lng_T}, ${prev_dis}"
        }
        return Triple(lat_T, lng_T, fix)
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
                    createNotification("피보호자의 위치를 받아오지 못했습니다.")
                    locationCount = 0
                }else{
                    mSocket.emit("requestLoc")
                    locationCount += 1
                }
            }
        }
    }

    private fun checkCell(cell: Int){
        if(cell != now_cell){
            now_cell = cell
            val next_cell = predictionPath(cell)
            Log.e("씨발", "now : ${now_cell}, next : ${next_cell}")
            if(next_cell == 0){
                createNotification("피보호자가 예측 경로를 벗어났습니다.")
                drawPath(-1, -1)
                App.prefs.isPred = false
            }else if(next_cell == -1) {
                drawPath(-1, -1)
                Log.e("예측", "예측종료")
                App.prefs.isPred = false
            }
            else{
                App.prefs.isPred = true
                drawPath(now_cell, next_cell)
            }
        }
    }

    private fun predictionPath(cell: Int): Int{
        var subList = mutableListOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        var next_cell = 0 ; var size = tmpData.size ; var i = 0
        var end_pred = false
        while(i < size){
            if(tmpData[i][0] != cell){
                tmpData.removeAt(i)
                i -= 1
                size -= 1
                end_pred = false
            }else{
                tmpData[i].removeAt(0)
                if(!tmpData[i].isEmpty()){
                    subList[tmpData[i][0]] += 1
                    end_pred = true
                }
            }
            i += 1
        }
        next_cell = subList.indexOf(subList.max())
        if(tmpData.size == 0){
            if(end_pred){
                return -1
            }else {
                return 0
            }
        }else{
            return next_cell
        }
    }

    private fun getCell(lat: Double, lng: Double): Int{
        var lat_cell = 37.58910900845096 ; var lng_cell = 127.0508933021918
        val lat_add = 0.0027030070953936 ; val lng_add = 0.0033958355736415
        var lat_num = 0 ; var lng_num = 1
        var cell_num = 0

        while(lat_cell > 37.56207893749702){
            if(lat in lat_cell-lat_add..lat_cell){
                break
            }else{
                lat_num += 1
                lat_cell -= lat_add
            }
        }
        while(lng_cell < 127.0848516579282){
            if(lng in lng_cell..lng_cell+lng_add){
                break
            }else{
                lng_num += 1
                lng_cell += lng_add
            }
        }
        if(lat_num > 10 || lng_num > 10){
            cell_num = 0
        }else{
            cell_num = (lat_num*10) + lng_num
        }
        return cell_num
    }

    private fun drawPath(now_cell: Int, next_cell: Int){
        App.prefs.now = now_cell
        App.prefs.next = next_cell
    }

    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ알람 관련ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun createNotification(msg: String){
        vibratorAlarm()
        val notification = NotificationFile.createNotification(this, msg)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun vibratorAlarm() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect = VibrationEffect.createOneShot(2000, 100)
        vibrator.vibrate(vibrationEffect)
    }
    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ암호화 관련ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    fun encrypt(input: String, key: ByteArray): String {
        var arr = ByteArray(16)
        var str = ""
        while(str.length != 16){
            SecureRandom().nextBytes(arr)
            str = String(arr)
        }
        var plainText = input.plus(getSign(input))
        Log.e("평뮨", input)
        Log.e("사인123", getSign(input))
        Log.e("플래인", plainText)
        val iv = getIv(str)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(key,"AES")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
        val encrypt = cipher.doFinal(plainText.toByteArray());
        return str + Base64Utils.encode(encrypt)
    }

    fun decrypt(input: String, key: ByteArray): String {
        var iv = getIv(input.substring(0, 16))
        var cryptText = input.substring(16, input.length)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(key,"AES")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
        val decrypt = cipher.doFinal(Base64Utils.decode(cryptText))
        return String(decrypt)
    }

    fun getIv(input: String): IvParameterSpec {
        var ivHash = MessageDigest.getInstance("SHA1")
            .digest(input.toByteArray())
        var ivBytes = Arrays.copyOf(ivHash, 16)
        var iv = IvParameterSpec(ivBytes)
        return iv
    }

    fun getKey(): ByteArray{
        var keyArr: ByteArray = App.prefs.key.toByteArray().slice(0..31).toByteArray()
        var pkStr = App.prefs.privateKey
        val pk = Base64Utils.decode(pkStr)

        val kf = KeyFactory.getInstance("RSA")
        val private = kf.generatePrivate(PKCS8EncodedKeySpec(pk))
        this.privateKey = private
        return keyArr
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
        return "SiGn"+Base64Utils.encode(hash)
    }

    fun verifSign(input: String): Boolean{
        var arr = input.split("SiGn")
        val cipherText = arr[0]
        val sign = arr[1]
        val hash = getSign(cipherText).substring(4)
        Log.e("싸인1", sign)
        Log.e("싸인2", hash)
        return hash == sign
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

        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    val networkListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {

        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    inner class timerReceiver: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent != null) {
                isSendCert = true
                var timer = TimerThread_G(true)
                timer.run()
            }
        }
    }

    inner class TimerThread_G(var isSendCert: Boolean): Thread(){
        override fun run(){
            sleep(5000)
            if(isCert){
                isCert = false
            }else{
                createNotification("피보호자가 인증을 수행하지 않았습니다.")
            }
            isSendCert = false
        }
    }

    inner class TimerThread_W: Thread(){
        override fun run(){
            sleep(5000)
            if(App.prefs.cert){
                createNotification("인증을 수행하지 않았습니다.")
            }
            App.prefs.cert = false
        }
    }
}

class alarmManagerReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent != null){
            var rIntent = Intent("alarm")
            val requestNum = intent.getIntExtra("num", 999999)
            var tIntent = Intent("timer")
            rIntent.putExtra("num", requestNum)
            Log.e("알람", "전송")
            App.mSocket.emit("sendCert")
            LocalBroadcastManager.getInstance(context!!).sendBroadcast(rIntent)
            LocalBroadcastManager.getInstance(context!!).sendBroadcast(tIntent)
        }
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