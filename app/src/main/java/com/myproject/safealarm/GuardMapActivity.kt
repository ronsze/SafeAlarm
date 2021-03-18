package com.myproject.safealarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.myproject.safealarm.databinding.ActivityGuardMapBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.GroundOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import java.io.IOException
import java.util.*

class GuardMapActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var binding: ActivityGuardMapBinding
    private lateinit var mapView: MapView
    private var onMaker = false

    var s_lat = App.prefs.s_lat.toDouble()
    var s_lng = App.prefs.s_lng.toDouble()

    val lat_1km: Double = 1.0/110.9875
    val lng_1km: Double = 1.0/88.3435

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
        changePosition(s_lat, s_lng)         //카메라 위치 변경
    }

    private fun setMarker(lat: Double, lng: Double){                                        //마커 생성
        val marker = Marker()
        marker.position = LatLng(lat, lng)  //마커 위치
        marker.icon = OverlayImage.fromResource(R.drawable.ic_baseline_place_24)    //마커 아이콘
        marker.alpha = 0.8f                                                         //마커 투명도
        marker.zIndex = 10                                                          //마커 우선순위
        marker.map = naverMap
        onMaker = true
    }

    private fun setOveray(lat: Double, lng: Double){
        val groundOverlay = GroundOverlay()
        groundOverlay.bounds = LatLngBounds(
                LatLng(lat-lat_1km, lng-lng_1km), LatLng(lat+lat_1km, lng+lng_1km))
        groundOverlay.image = OverlayImage.fromResource(R.drawable.rect_map)
        groundOverlay.alpha = 0.7f
        groundOverlay.map = naverMap
    }

    private fun changePosition(latitude: Double, longitude: Double) {                               //카메라 위치 변경
        val cameraPosition = CameraPosition(
                LatLng(latitude, longitude),                                            //좌표
                15.0                                                              //줌 레벨
        )
        cngLocation(latitude, longitude)
        setMarker(latitude, longitude)
        setOveray(latitude, longitude)
        naverMap.cameraPosition = cameraPosition
    }

    private fun cngLocation(latitude: Double, longitude: Double){           //위도, 경도를 주소로 변경
        val mGeocoder = Geocoder(applicationContext, Locale.KOREAN)
        var mResultList: List<Address>? = null
        var currentLocation = ""
        try{
            mResultList = mGeocoder.getFromLocation(
                    latitude, longitude, 1
            )
        }catch(e: IOException){
            e.printStackTrace()
        }
        if(mResultList != null){
            currentLocation = mResultList[0].getAddressLine(0)
            currentLocation = currentLocation.substring(5)
        }
        binding.locationTxt.setText(currentLocation)
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