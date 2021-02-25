package com.myproject.safealarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.NonNull
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback

class GuardMapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mapView: MapView
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
        setContentView(R.layout.activity_guard_map)

    }

    override fun onMapReady(naverMap: NaverMap) {
        GuardMapActivity.naverMap = naverMap
        naverMap.setMapType(NaverMap.MapType.Satellite)
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)
        var cameraPosition = CameraPosition(
                LatLng(33.38, 126.55),  //좌표
                9.0,                               //줌 레벨
                45.0,                                //각도
                45.0                             // 방향
        )
        naverMap.cameraPosition = cameraPosition
    }


}