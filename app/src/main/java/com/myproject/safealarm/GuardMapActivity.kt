package com.myproject.safealarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.myproject.safealarm.databinding.ActivityGuardMapBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage

class GuardMapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    lateinit var binding: ActivityGuardMapBinding
    val marker = Marker()
    companion object{
        lateinit var naverMap: NaverMap

        fun changePosition(latitude: Double, longitude: Double){
            var cameraPosition = CameraPosition(
                    LatLng(latitude, longitude),  //좌표
                    9.0                              //줌 레벨
            )
            naverMap.cameraPosition = cameraPosition
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardMapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(naverMap: NaverMap) {
        GuardMapActivity.naverMap = naverMap
        naverMap.setMapType(NaverMap.MapType.Basic);
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)
        var cameraPosition = CameraPosition(
                LatLng(37.572404625825584, 127.06839948892593),  //좌표
                15.0                                                         //줌 레벨
        )
        naverMap.cameraPosition = cameraPosition
        setMarker(37.572404625825584, 127.06839948892593, 0.8f, R.drawable.ic_baseline_place_24, 10)
    }

    private fun setMarker(lat: Double, lng: Double, alpha: Float, resourceID: Int, zIndex: Int){
        Log.d("좌표", "${lat}, ${lng}")
        marker.position = LatLng(lat, lng)  //마커 위치
        marker.icon = OverlayImage.fromResource(resourceID)     //마커 아이콘
        marker.alpha = alpha    //마커 투명도
        marker.zIndex = zIndex  //마커 우선순위
        marker.map = naverMap
    }

}