package com.myproject.safealarm

import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.myproject.safealarm.databinding.ActivityRangeAddBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import java.io.IOException
import java.util.*

class RangeAddActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityRangeAddBinding
    private lateinit var mapView: MapView
    private lateinit var marker: Marker
    private var canAdd = false
    companion object {
        lateinit var naverMap: NaverMap
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRangeAddBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        marker = Marker()
        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        spinnerSet()

        binding.add.setOnClickListener {
            if(canAdd){
                Toast.makeText(this, "${binding.rangeSpin.selectedItem.toString()}", Toast.LENGTH_SHORT).show()
                finish()
            }else{
                Toast.makeText(this, "추가할 수 없습니다.\n주소를 다시 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.cancle.setOnClickListener {
            finish()
        }
        binding.addressBtn.setOnClickListener {
            cngLocation()
        }
    }

    private fun spinnerSet(){
        val rAdapter = ArrayAdapter.createFromResource(this, R.array.range, R.layout.spinner_font_range)
        rAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.rangeSpin.adapter = rAdapter
        binding.rangeSpin.setSelection(0)
    }

    override fun onMapReady(naverMap: NaverMap) {                                           //지도 최초 생성
        RangeAddActivity.naverMap = naverMap
        naverMap.setMapType(NaverMap.MapType.Basic);                                    //지도 뒷 배경
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)      //건물 표시

        val cameraPosition = CameraPosition(
            LatLng(37.577, 126.976),                                                       //좌표
            13.0                                                                 //줌 레벨
        )
        RangeAddActivity.naverMap.cameraPosition = cameraPosition
    }

    private fun moveMaker(lat: Double, lng: Double){
        marker.position = LatLng(lat, lng)  //마커 위치
        marker.icon = OverlayImage.fromResource(R.drawable.ic_baseline_place_24)    //마커 아이콘
        marker.alpha = 0.8f                                                         //마커 투명도
        marker.zIndex = 10                                                          //마커 우선순위
        marker.map = RangeAddActivity.naverMap

        val cameraPosition = CameraPosition(
            LatLng(lat, lng),                                   //좌표
            13.0                                                                 //줌 레벨
        )
        RangeAddActivity.naverMap.cameraPosition = cameraPosition
    }

    private fun cngLocation() {           //위도, 경도를 주소로 변경
        val address = binding.addressText.text.toString().replace("\\s".toRegex(), "")
        val mGeocoder = Geocoder(this, Locale.KOREAN)
        var list: List<Address>? = null
        try {
            list = mGeocoder.getFromLocationName(address, 10)
            canAdd = true
        } catch (e: IOException) {
            e.printStackTrace()
            canAdd = false
        }
        if(list == null || list.size <= 0){
            Toast.makeText(this, "일치하는 주소가 없습니다.", Toast.LENGTH_SHORT).show()
        }else{
            moveMaker(list.get(0).latitude, list.get(0).longitude)
            Toast.makeText(this, "${binding.addressText.text.toString()}", Toast.LENGTH_SHORT).show()
        }
    }
}