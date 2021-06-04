package com.myproject.safealarm

import android.Manifest
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
import java.lang.RuntimeException
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.*

class ForegroundService : Service() {
    private val role = App.prefs.role
    private val MAX_REQUEST_COUNT = 12


    private var locationManager: LocationManager? = null
    private var latSave: Double = 0.0 ; private var lngSave: Double = 0.0
    private var locationCount = 0
    private var prevLat = 0.0 ; private var prevLng = 0.0 ; private var prevDis = 0.0
    private var arr: FloatArray = floatArrayOf(1.0f)
    private var isFixS = false ; private var nowCell = 0
    private var isSendCert = false ; private var isCert = false ; private var first = false
    //private val lat1km: Double = 1.0 / 110.9875 ; private val lng1km: Double = 1.0 / 88.3435

    private lateinit var shardKey: ByteArray
    private lateinit var privateKey: PrivateKey
    private lateinit var socketT: socketThread


    private var entirePathData: MutableList<MutableList<Int>> = mutableListOf(
        mutableListOf(56,57,58,68,67,66,56),
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
    private var tmpPathData: MutableList<MutableList<Int>> = mutableListOf()

    companion object {
        val mSocket = App.mSocket
        const val NOTIFICATION_ID = 20
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START_FOREGROUND -> {
                startForegroundService()
            }
            Actions.STOP_FOREGROUND -> {
                stopForegroundService()
            }
            Actions.HELP_CALL_WARD -> {
                mSocket.emit("HelpCall_W")
            }
        }

        shardKey = getKey()
        checkSocketConneted()
        copyEntirePathData()
        registLocationReceiver()
        registLocalBCReceiver()

        return START_STICKY
    }

    //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ통신 관련ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun checkSocketConneted(){
        if (mSocket.connected()) {
            connectSocket()
        }
    }

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
            mSocket.on("newMissing", onNewMissing)
            if (role == "Guard") {
                sendLocReq()
            }
        }
    }

    private fun copyEntirePathData(){
        if (App.prefs.role == "Guard") {
            tmpPathData.addAll(entirePathData)
        }
    }

    private fun registLocationReceiver(){
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
    }

    private fun registLocalBCReceiver(){
        LocalBroadcastManager.getInstance(this).registerReceiver(alarmManagerReceiver(), IntentFilter("Alarm"))
        LocalBroadcastManager.getInstance(this).registerReceiver(timerReceiver(), IntentFilter("timer"))
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
            val msg = it[0].toString()
            try{
                if(checkSign(msg, getPublicKey())){
                    getLatLng()
                }else{
                    Log.e("Receive 위치 요청 서명검증", "실패")
                }
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
            startVerifyTimer_W()
        }
    }

    private fun startVerifyTimer_W(){
        CoroutineScope(Dispatchers.Main).launch {
            delay(3*60*1000)
            if(App.prefs.cert){
                createNotification("인증을 수행하지 않았습니다.")
            }
            App.prefs.cert = false
        }
    }

    private val onCallbackCert = Emitter.Listener {            //인증 메세지 완료
        if(role == "Guard"){
            if(isSendCert){
                Log.e("인증 수행 완료", "완료")
                createNotification("피보호자가 인증을 완료했습니다.")
                isCert = true
            }
        }else if(role == "Ward"){
            createNotification("인증을 수행했습니다.")
        }

    }

    private val onCallbackLoc = Emitter.Listener {              //좌표 받음
        if (role == "Guard") {
            var location = it[0].toString()
            try {
                val `object` = JSONObject(location)
                val deLat = AESDecrypt(`object`.getString("latitude"), shardKey)
                val deLng = AESDecrypt(`object`.getString("longitude"), shardKey)
                if(checkSign(deLat, getPublicKey()) && checkSign(deLng, getPublicKey())){
                    val latitude = deLat.split("SiGn")[0].toDouble()
                    val longitude = deLng.split("SiGn")[0].toDouble()
                    if (latitude == 0.0 && longitude == 0.0) {
                        locationCount += 1
                    } else {
                        locationCount = 0
                        `sendCngMapBroadCast`(latitude, longitude)
                        first = true
                        var cell = getCell(latitude, longitude)
                        if (first) {
                            checkCell(cell)
                        }
                    }
                }else{
                    Log.e("Receive Location 서명 검증", "실패")
                    locationCount += 1
                }
            } catch (e: JSONException) {
                locationCount += 1
                e.printStackTrace()
            }
        }
    }

    private val onNewMissing = Emitter.Listener {
        createNotification("새로운 실종정보가 등록되었습니다.")
    }

//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ위치 관련ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private fun getLatLng() {                                   //피보호자 좌표 구하는 함수
        var lat = 0.0 ; var lng = 0.0
        var location: Location? = null
        var providerStr: String? = null ; var fix = "보정없음"
        val isGPSEnable = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnable = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        var json = JSONObject()

        if (!isGPSEnable && !isNetworkEnable) {
            providerStr = providerStr.plus("error1")
        } else {
            val hasFinePer = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            val hasCoarsePer = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            if (hasFinePer == PackageManager.PERMISSION_GRANTED &&
                hasCoarsePer == PackageManager.PERMISSION_GRANTED) {
                val locG: Location? = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val locN: Location? = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (locationManager != null) {
                    if(locG != null && locN != null){
                        location = getLocatioObject(locG, locN)
                        providerStr = location.provider
                    }
                    if (location != null) {
                        val locTriple = getLocation(location)
                        lat = locTriple.first
                        lng = locTriple.second
                        fix = locTriple.third

                        json.put("accuracy", location.accuracy)
                        json.put("speed", location.speed)
                    } else {
                        providerStr = providerStr.plus("error2")
                    }
                } else {
                    providerStr = providerStr.plus("error3")
                }
            } else { }
        }
        try {
            var enLat = AESEncrypt(lat.toString(), shardKey)
            var enLng = AESEncrypt(lng.toString(), shardKey)
            json.put("provider", providerStr)
            json.put("latitude", enLat)
            json.put("longitude", enLng)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mSocket.emit("callbackLoc", json)
    }

    private fun getLocatioObject(locG: Location, locN: Location): Location{
        if(locG.accuracy <= locN.accuracy){
            return locG
        }else{
            return locN
        }
    }

    private fun getLocation(location: Location): Triple<Double, Double, String>{
        var lat = 0.0   ;   var lng = 0.0   ;   var fix = ""
        if((latSave == 0.0 && lngSave == 0.0)){
            lat = location.latitude
            lng = location.longitude
            latSave = lat ; lngSave = lng
        }
        else{
            val triple: Triple<Double, Double, String> = GPS_Filter(location)
            lat = triple.first
            lng = triple.second
            fix = triple.third

            if(lat - latSave == 0.0 && lng - lngSave == 0.0){ }
            else if(isFixS) {
                prevDis = 0.0
            }
            else {
                prevLat = lat - latSave ; prevLng = lng - lngSave
                Location.distanceBetween(latSave, lngSave, lat, lng, arr)
                prevDis = abs(arr[0]).toDouble()
            }
            latSave = lat ; lngSave = lng
        }

        return Triple(lat, lng, fix)
    }

    private fun GPS_Filter(location: Location): Triple<Double, Double, String> {
        var first = location.latitude ; var second = location.longitude ; var third = location.speed
        var latF: Double = first ; var latS: Double = first ; var latT: Double = first
        var lngF: Double = second ; var lngS: Double = second ; var lngT: Double = second
        var fix = "보정없음" ; var isFix = false
        var speed = third * 5.0
        val subLat = first - latSave ; val subLng = second - lngSave
        var ratioDis = 0.0

        var arr: FloatArray = floatArrayOf(1.0f)
        Location.distanceBetween(latSave, lngSave, first, second, arr)
        val distance = abs(arr[0])

        if(distance >= speed + 5.0){
            if(speed == 0.0) speed = 5.0
            else speed += 2.0

            ratioDis = speed/distance
            latF = latSave + subLat * ratioDis
            lngF = lngSave + subLng * ratioDis

            if(prevDis == 0.0){
                isFixS = false
                latT = latF
                lngT = lngF
            }else {
                ratioDis = prevDis / speed
                latS = latSave + prevLat * ratioDis
                lngS = lngSave + prevLng * ratioDis
                isFixS = true
                latT = (latF + latS) / 2.0
                lngT = (lngF + lngS) / 2.0
            }

            isFix = true
        }
        if(isFix){
            fix = "보정 : ${first} -> ${latT}, ${second} -> ${lngT}, ${prevDis}"
        }
        return Triple(latT, lngT, fix)
    }

    private fun sendCngMapBroadCast(latitude: Double, longitude: Double) {       //위치 변경 브로드캐스트
        var intent = Intent(App.CNG_LOC)
        intent.putExtra("latitude", latitude)
        intent.putExtra("longitude", longitude)
        App.prefs.saveLat = latitude.toString()
        App.prefs.saveLng = longitude.toString()
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendLocReq() {                                              //위치 요청
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(5000)
                if (checkNoResponseCnt()) {
                    sendNoResponseMaxMsg()
                }else{
                    val randomText = getRandomText(32).substring(0, 32)
                    mSocket.emit("requestLoc", randomText+getSign(randomText))
                    locationCount += 1
                }
            }
        }
    }

    private fun checkNoResponseCnt(): Boolean{
        return locationCount >= MAX_REQUEST_COUNT
    }

    private fun sendNoResponseMaxMsg(){
        Log.e("위치 요청", "응답 없음 1분")
        createNotification("피보호자의 위치를 받아오지 못했습니다.")
        locationCount = 0
    }

    private fun getRandomText(size: Int): String{
        var randomArray = ByteArray(size)
        SecureRandom().nextBytes(randomArray)
        var randomText = Base64Utils.encode(randomArray)

        return randomText
    }

    private fun checkCell(cell: Int){
        if(cell != nowCell){
            nowCell = cell
            val nextCell = predictionPath(cell)
            if(nextCell == 0){
                sendEscapePathMsg()
            }else if(nextCell == -1) {
                sendPredDoneMsg()
            }else{
                drawNextPath(nextCell)
            }
        }
    }

    private fun sendEscapePathMsg(){
        createNotification("피보호자가 예측 경로를 벗어났습니다.")
        drawPath(-1, -1)
        App.prefs.isPred = false
    }

    private fun sendPredDoneMsg(){
        createNotification("예측이 종료되었습니다.")
        drawPath(-1, -1)
        App.prefs.isPred = false
    }

    private fun drawNextPath(nextCell: Int){
        App.prefs.isPred = true
        drawPath(nowCell, nextCell)
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
        var nextCell = 0 ; var size = tmpPathData.size ; var i = 0
        var endPred = false
        while(i < size){
            if(tmpPathData[i][0] != cell){
                tmpPathData.removeAt(i)
                i -= 1
                size -= 1
                endPred = false
            }else{
                tmpPathData[i].removeAt(0)
                if(!tmpPathData[i].isEmpty()){
                    subList[tmpPathData[i][0]] += 1
                    endPred = true
                }
            }
            i += 1
        }
        nextCell = subList.indexOf(subList.max())
        if(tmpPathData.size == 0){
            if(endPred){
                return -1
            }else {
                return 0
            }
        }else{
            return nextCell
        }
    }

    private fun getCell(lat: Double, lng: Double): Int{
        var latCell = 37.58910900845096 ; var lngCell = 127.049195384405
        val latAdd = 0.0027030070953936 ; val lngAdd = 0.0033958355736415
        var latNum = 0 ; var lngNum = 1
        var cellNum = 0

        while(latCell > 37.56207893749702){
            if(lat in latCell-latAdd..latCell){
                break
            }else{
                latNum += 1
                latCell -= latAdd
            }
        }
        while(lngCell < 127.0831537401414){
            if(lng in lngCell..lngCell+lngAdd){
                break
            }else{
                lngNum += 1
                lngCell += lngAdd
            }
        }
        if(latNum > 10 || lngNum > 10){
            cellNum = 0
        }else{
            cellNum = (latNum*10) + lngNum
        }
        return cellNum
    }

    private fun drawPath(now_cell: Int, next_cell: Int){
        App.prefs.nowCell = now_cell
        App.prefs.nextCell = next_cell
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
    private fun AESEncrypt(input: String, key: ByteArray): String {
        var randomText = getRandomText(32).substring(0, 16)
        var plainText = input.plus(getSign(input))
        val iv = getIv(randomText)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(key,"AES")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
        val encrypt = cipher.doFinal(plainText.toByteArray());
        return randomText + Base64Utils.encode(encrypt)
    }

    private fun AESDecrypt(input: String, key: ByteArray): String {
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
        var keyArr: ByteArray = App.prefs.shardKey.toByteArray().slice(0..31).toByteArray()
        var pkStr = App.prefs.privateKey
        val pk = Base64Utils.decode(pkStr)

        val kf = KeyFactory.getInstance("RSA")
        val private = kf.generatePrivate(PKCS8EncodedKeySpec(pk))
        this.privateKey = private
        return keyArr
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
        override fun onLocationChanged(location: Location) {}
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    val networkListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {}
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    inner class timerReceiver: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            CoroutineScope(Dispatchers.Main).launch {
                try{
                    if(intent != null) {
                        isSendCert = true
                        delay(3*60*1000)
                        if(isCert){
                            isCert = false
                        }else{
                            createNotification("피보호자가 인증을 수행하지 않았습니다.")
                        }
                        isSendCert = false
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }
}

class alarmManagerReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent != null){
            if(context != null){
                App.mSocket.emit("sendCert")
                sendReRegistAlarm(intent, context)
                sendTimerBroadCast(context)
            }
        }
    }

    private fun sendReRegistAlarm(intent: Intent, context: Context){
        val rIntent = Intent("alarm")

        val requestNum = intent.getIntExtra("num", 999999)
        val hour = intent.getStringExtra("hour")
        val min = intent.getStringExtra("min")

        rIntent.putExtra("num", requestNum)
        rIntent.putExtra("hour", hour)
        rIntent.putExtra("min", min)

        LocalBroadcastManager.getInstance(context).sendBroadcast(rIntent)
    }

    private fun sendTimerBroadCast(context: Context){
        val tIntent = Intent("timer")
        LocalBroadcastManager.getInstance(context).sendBroadcast(tIntent)
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