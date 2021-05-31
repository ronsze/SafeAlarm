package com.myproject.safealarm

import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.myproject.safealarm.databinding.ActivityMissingMapBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import java.io.IOException
import java.util.*

class MissingMapActivity : AppCompatActivity(), OnMapReadyCallback {
    private val context = this

    private lateinit var binding: ActivityMissingMapBinding
    private lateinit var mapView: MapView
    private lateinit var marker: Marker

    companion object {
        lateinit var naverMap: NaverMap
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMissingMapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        marker = Marker()
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(naverMap: NaverMap) {                                           //지도 최초 생성
        MissingMapActivity.naverMap = naverMap
        naverMap.setMapType(NaverMap.MapType.Basic);                                    //지도 뒷 배경
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)      //건물 표시

        val cameraPosition = CameraPosition(
            LatLng(37.577, 126.976),                                                       //좌표
            13.0                                                                 //줌 레벨
        )
        MissingMapActivity.naverMap.cameraPosition = cameraPosition
        setLocation()
    }

    private fun setLocation(){
        val mIntent = this.intent
        if(mIntent != null){
            var loc = mIntent.getStringExtra("loc")
            if(loc != null){
                val location = textToLocation(loc, context)
                val lat = location.first  ; val lng = location.second
                if(lat == 0.0 && lng == 0.0){
                    Toast.makeText(this, "일치하는 주소가 없습니다.", Toast.LENGTH_SHORT).show()
                }else{
                    moveMaker(lat, lng)
                }

            }
        }
    }

    private fun moveMaker(lat: Double, lng: Double){
        marker.position = LatLng(lat, lng)  //마커 위치
        marker.icon = OverlayImage.fromResource(R.drawable.ic_baseline_place_24)    //마커 아이콘
        marker.alpha = 0.8f                                                         //마커 투명도
        marker.zIndex = 10                                                          //마커 우선순위
        marker.map = MissingMapActivity.naverMap

        val cameraPosition = CameraPosition(
            LatLng(lat, lng),                                   //좌표
            13.0                                                                 //줌 레벨
        )
        MissingMapActivity.naverMap.cameraPosition = cameraPosition
    }
}