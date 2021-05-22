package com.myproject.safealarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.myproject.safealarm.databinding.ActivityGuardMapBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.ArrowheadPathOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import java.io.IOException
import java.util.*

class GuardMapActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var binding: ActivityGuardMapBinding
    private lateinit var mapView: MapView

    private var s_lat = App.prefs.s_lat.toDouble()
    private var s_lng = App.prefs.s_lng.toDouble()
    private val marker = Marker()
    private val center_latLng: List<List<Double>> = listOf(
        listOf(37.58775750490326, 127.05259121997861),
        listOf(37.58775750490326, 127.05598705555225),
        listOf(37.58775750490326, 127.05938289112589),
        listOf(37.58775750490326, 127.06277872669953),
        listOf(37.58775750490326, 127.06617456227318),
        listOf(37.58775750490326, 127.06957039784682),
        listOf(37.58775750490326, 127.07296623342046),
        listOf(37.58775750490326, 127.0763620689941),
        listOf(37.58775750490326, 127.07975790456774),
        listOf(37.58775750490326, 127.08315374014138),
        listOf(37.58505449780787, 127.05259121997861),
        listOf(37.58505449780787, 127.05598705555225),
        listOf(37.58505449780787, 127.05938289112589),
        listOf(37.58505449780787, 127.06277872669953),
        listOf(37.58505449780787, 127.06617456227318),
        listOf(37.58505449780787, 127.06957039784682),
        listOf(37.58505449780787, 127.07296623342046),
        listOf(37.58505449780787, 127.0763620689941),
        listOf(37.58505449780787, 127.07975790456774),
        listOf(37.58505449780787, 127.08315374014138),
        listOf(37.58235149071248, 127.05259121997861),
        listOf(37.58235149071248, 127.05598705555225),
        listOf(37.58235149071248, 127.05938289112589),
        listOf(37.58235149071248, 127.06277872669953),
        listOf(37.58235149071248, 127.06617456227318),
        listOf(37.58235149071248, 127.06957039784682),
        listOf(37.58235149071248, 127.07296623342046),
        listOf(37.58235149071248, 127.0763620689941),
        listOf(37.58235149071248, 127.07975790456774),
        listOf(37.58235149071248, 127.08315374014138),
        listOf(37.57964848361709, 127.05259121997861),
        listOf(37.57964848361709, 127.05598705555225),
        listOf(37.57964848361709, 127.05938289112589),
        listOf(37.57964848361709, 127.06277872669953),
        listOf(37.57964848361709, 127.06617456227318),
        listOf(37.57964848361709, 127.06957039784682),
        listOf(37.57964848361709, 127.07296623342046),
        listOf(37.57964848361709, 127.0763620689941),
        listOf(37.57964848361709, 127.07975790456774),
        listOf(37.57964848361709, 127.08315374014138),
        listOf(37.576945476521696, 127.05259121997861),
        listOf(37.576945476521696, 127.05598705555225),
        listOf(37.576945476521696, 127.05938289112589),
        listOf(37.576945476521696, 127.06277872669953),
        listOf(37.576945476521696, 127.06617456227318),
        listOf(37.576945476521696, 127.06957039784682),
        listOf(37.576945476521696, 127.07296623342046),
        listOf(37.576945476521696, 127.0763620689941),
        listOf(37.576945476521696, 127.07975790456774),
        listOf(37.576945476521696, 127.08315374014138),
        listOf(37.574242469426305, 127.05259121997861),
        listOf(37.574242469426305, 127.05598705555225),
        listOf(37.574242469426305, 127.05938289112589),
        listOf(37.574242469426305, 127.06277872669953),
        listOf(37.574242469426305, 127.06617456227318),
        listOf(37.574242469426305, 127.06957039784682),
        listOf(37.574242469426305, 127.07296623342046),
        listOf(37.574242469426305, 127.0763620689941),
        listOf(37.574242469426305, 127.07975790456774),
        listOf(37.574242469426305, 127.08315374014138),
        listOf(37.57153946233091, 127.05259121997861),
        listOf(37.57153946233091, 127.05598705555225),
        listOf(37.57153946233091, 127.05938289112589),
        listOf(37.57153946233091, 127.06277872669953),
        listOf(37.57153946233091, 127.06617456227318),
        listOf(37.57153946233091, 127.06957039784682),
        listOf(37.57153946233091, 127.07296623342046),
        listOf(37.57153946233091, 127.0763620689941),
        listOf(37.57153946233091, 127.07975790456774),
        listOf(37.57153946233091, 127.08315374014138),
        listOf(37.56883645523552, 127.05259121997861),
        listOf(37.56883645523552, 127.05598705555225),
        listOf(37.56883645523552, 127.05938289112589),
        listOf(37.56883645523552, 127.06277872669953),
        listOf(37.56883645523552, 127.06617456227318),
        listOf(37.56883645523552, 127.06957039784682),
        listOf(37.56883645523552, 127.07296623342046),
        listOf(37.56883645523552, 127.0763620689941),
        listOf(37.56883645523552, 127.07975790456774),
        listOf(37.56883645523552, 127.08315374014138),
        listOf(37.56613344814013, 127.05259121997861),
        listOf(37.56613344814013, 127.05598705555225),
        listOf(37.56613344814013, 127.05938289112589),
        listOf(37.56613344814013, 127.06277872669953),
        listOf(37.56613344814013, 127.06617456227318),
        listOf(37.56613344814013, 127.06957039784682),
        listOf(37.56613344814013, 127.07296623342046),
        listOf(37.56613344814013, 127.0763620689941),
        listOf(37.56613344814013, 127.07975790456774),
        listOf(37.56613344814013, 127.08315374014138),
        listOf(37.56343044104474, 127.05259121997861),
        listOf(37.56343044104474, 127.05598705555225),
        listOf(37.56343044104474, 127.05938289112589),
        listOf(37.56343044104474, 127.06277872669953),
        listOf(37.56343044104474, 127.06617456227318),
        listOf(37.56343044104474, 127.06957039784682),
        listOf(37.56343044104474, 127.07296623342046),
        listOf(37.56343044104474, 127.0763620689941),
        listOf(37.56343044104474, 127.07975790456774),
        listOf(37.56343044104474, 127.08315374014138)
    )
    private var next_cell = App.prefs.next ; private var isPred = false
    val arrowheadPath = ArrowheadPathOverlay()

    companion object {
        lateinit var naverMap: NaverMap
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardMapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        s_lat = App.prefs.s_lat.toDouble()
        s_lng = App.prefs.s_lng.toDouble()

        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        LocalBroadcastManager.getInstance(this).registerReceiver(locReceiver(), IntentFilter(App.CNG_LOC))

    }

    override fun onMapReady(naverMap: NaverMap) {                                           //지도 최초 생성
        GuardMapActivity.naverMap = naverMap
        naverMap.setMapType(NaverMap.MapType.Basic);                                    //지도 뒷 배경
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)      //건물 표시

        val cameraPosition = CameraPosition(
            LatLng(s_lat, s_lng),                                                       //좌표
            15.0                                                                 //줌 레벨
        )
        Companion.naverMap.cameraPosition = cameraPosition
        changePosition(s_lat, s_lng)                                                    //마커 위치 변경, 주소 변경
    }

    private fun setMarker(lat: Double, lng: Double){                                        //마커 생성
        marker.position = LatLng(lat, lng)  //마커 위치
        marker.icon = OverlayImage.fromResource(R.drawable.ic_baseline_place_24)    //마커 아이콘
        marker.alpha = 0.8f                                                         //마커 투명도
        marker.zIndex = 10                                                          //마커 우선순위
        marker.map = naverMap
    }

    private fun changePosition(latitude: Double, longitude: Double) {                               //마커, 주소 최신화
        //cngLocation(latitude, longitude)
        setMarker(latitude, longitude)
        val cameraPosition = CameraPosition(
            LatLng(latitude, longitude),
            naverMap.cameraPosition.zoom
        )
        cngLocation(latitude, longitude)
        naverMap.cameraPosition = cameraPosition
        isPred = App.prefs.isPred
        if(isPred){
            next_cell = App.prefs.next
            drawPath(Pair(latitude, longitude), next_cell)
        }
    }

    private fun drawPath(latLng: Pair<Double, Double>, next_cell: Int){
        var next_latLng = center_latLng[next_cell-1]
        arrowheadPath.coords = listOf(
            LatLng(latLng.first, latLng.second),
            LatLng(next_latLng[0], next_latLng[1])
        )
        arrowheadPath.map = naverMap
        Log.e("셀 번호", "다음 셀 : ${next_cell}")
        Log.e("셀 좌표", "다음 셀 : ${next_latLng}")
    }

    private fun cngLocation(latitude: Double, longitude: Double){           //위도, 경도를 주소로 변경
        var lat_s = latitude.toString() ; var lng_s = longitude.toString()
        var lat = lat_s.toDouble()
        var lng = lng_s.toDouble()
        if(lat_s.length >= 7){
            lat_s.substring(0, 6).toDouble()
        }
        if(lng_s.length >= 9){
            lng = lng_s.substring(0, 8).toDouble()
        }
        val mGeocoder = Geocoder(this, Locale.KOREAN)
        var mResultList: List<Address>? = null
        var currentLocation = ""
        try{
            mResultList = mGeocoder.getFromLocation(
                lat, lng, 1
            )
        }catch(e: IOException){
            e.printStackTrace()
        }
        if(mResultList != null){
            currentLocation = mResultList[0].getAddressLine(0)
            currentLocation = currentLocation.substring(5)
        }
        binding.locationTxt.setText(currentLocation)
        Log.d("현재 위치", currentLocation)
    }

    inner class locReceiver: BroadcastReceiver(){                                           //브로드 캐스트 리시버
        override fun onReceive(context: Context?, intent: Intent?) {
            val latitude = intent?.getDoubleExtra("latitude", App.prefs.s_lat.toDouble())
            val longitude = intent?.getDoubleExtra("longitude", App.prefs.s_lng.toDouble())
            if(latitude != null && longitude != null){
                changePosition(latitude, longitude)
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locReceiver())
        super.onDestroy()
    }
}