package com.myproject.safealarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.myproject.safealarm.databinding.ActivityGuardMapBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.ArrowheadPathOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage

class GuardMapActivity : AppCompatActivity(), OnMapReadyCallback {
    private val context = this
    private val arrowheadPath = ArrowheadPathOverlay()
    private val marker = Marker()
    private var nowAdd = "" ; private var prevAdd = ""
    private var sLat = App.prefs.saveLat.toDouble()
    private var sLng = App.prefs.saveLng.toDouble()
    private val CENTER_LAT_LNG: List<List<Double>> = listOf(
        listOf(37.58775750490326, 127.05089330219178),
        listOf(37.58775750490326, 127.05428913776542),
        listOf(37.58775750490326, 127.05768497333906),
        listOf(37.58775750490326, 127.06108080891270),
        listOf(37.58775750490326, 127.06447664448635),
        listOf(37.58775750490326, 127.06787248005999),
        listOf(37.58775750490326, 127.07126831563363),
        listOf(37.58775750490326, 127.07466415120727),
        listOf(37.58775750490326, 127.07805998678091),
        listOf(37.58775750490326, 127.08145582235455),
        listOf(37.58505449780787, 127.05089330219178),
        listOf(37.58505449780787, 127.05428913776542),
        listOf(37.58505449780787, 127.05768497333906),
        listOf(37.58505449780787, 127.06108080891270),
        listOf(37.58505449780787, 127.06447664448635),
        listOf(37.58505449780787, 127.06787248005999),
        listOf(37.58505449780787, 127.07126831563363),
        listOf(37.58505449780787, 127.07466415120727),
        listOf(37.58505449780787, 127.07805998678091),
        listOf(37.58505449780787, 127.08145582235455),
        listOf(37.58235149071248, 127.05089330219178),
        listOf(37.58235149071248, 127.05428913776542),
        listOf(37.58235149071248, 127.05768497333906),
        listOf(37.58235149071248, 127.06108080891270),
        listOf(37.58235149071248, 127.06447664448635),
        listOf(37.58235149071248, 127.06787248005999),
        listOf(37.58235149071248, 127.07126831563363),
        listOf(37.58235149071248, 127.07466415120727),
        listOf(37.58235149071248, 127.07805998678091),
        listOf(37.58235149071248, 127.08145582235455),
        listOf(37.57964848361709, 127.05089330219178),
        listOf(37.57964848361709, 127.05428913776542),
        listOf(37.57964848361709, 127.05768497333906),
        listOf(37.57964848361709, 127.06108080891270),
        listOf(37.57964848361709, 127.06447664448635),
        listOf(37.57964848361709, 127.06787248005999),
        listOf(37.57964848361709, 127.07126831563363),
        listOf(37.57964848361709, 127.07466415120727),
        listOf(37.57964848361709, 127.07805998678091),
        listOf(37.57964848361709, 127.08145582235455),
        listOf(37.576945476521696, 127.05089330219178),
        listOf(37.576945476521696, 127.05428913776542),
        listOf(37.576945476521696, 127.05768497333906),
        listOf(37.576945476521696, 127.06108080891270),
        listOf(37.576945476521696, 127.06447664448635),
        listOf(37.576945476521696, 127.06787248005999),
        listOf(37.576945476521696, 127.07126831563363),
        listOf(37.576945476521696, 127.07466415120727),
        listOf(37.576945476521696, 127.07805998678091),
        listOf(37.576945476521696, 127.08145582235455),
        listOf(37.574242469426305, 127.05089330219178),
        listOf(37.574242469426305, 127.05428913776542),
        listOf(37.574242469426305, 127.05768497333906),
        listOf(37.574242469426305, 127.06108080891270),
        listOf(37.574242469426305, 127.06447664448635),
        listOf(37.574242469426305, 127.06787248005999),
        listOf(37.574242469426305, 127.07126831563363),
        listOf(37.574242469426305, 127.07466415120727),
        listOf(37.574242469426305, 127.07805998678091),
        listOf(37.574242469426305, 127.08145582235455),
        listOf(37.57153946233091, 127.05089330219178),
        listOf(37.57153946233091, 127.05428913776542),
        listOf(37.57153946233091, 127.05768497333906),
        listOf(37.57153946233091, 127.06108080891270),
        listOf(37.57153946233091, 127.06447664448635),
        listOf(37.57153946233091, 127.06787248005999),
        listOf(37.57153946233091, 127.07126831563363),
        listOf(37.57153946233091, 127.07466415120727),
        listOf(37.57153946233091, 127.07805998678091),
        listOf(37.57153946233091, 127.08145582235455),
        listOf(37.56883645523552, 127.05089330219178),
        listOf(37.56883645523552, 127.05428913776542),
        listOf(37.56883645523552, 127.05768497333906),
        listOf(37.56883645523552, 127.06108080891270),
        listOf(37.56883645523552, 127.06447664448635),
        listOf(37.56883645523552, 127.06787248005999),
        listOf(37.56883645523552, 127.07126831563363),
        listOf(37.56883645523552, 127.07466415120727),
        listOf(37.56883645523552, 127.07805998678091),
        listOf(37.56883645523552, 127.08145582235455),
        listOf(37.56613344814013, 127.05089330219178),
        listOf(37.56613344814013, 127.05428913776542),
        listOf(37.56613344814013, 127.05768497333906),
        listOf(37.56613344814013, 127.06108080891270),
        listOf(37.56613344814013, 127.06447664448635),
        listOf(37.56613344814013, 127.06787248005999),
        listOf(37.56613344814013, 127.07126831563363),
        listOf(37.56613344814013, 127.07466415120727),
        listOf(37.56613344814013, 127.07805998678091),
        listOf(37.56613344814013, 127.08145582235455),
        listOf(37.56343044104474, 127.05089330219178),
        listOf(37.56343044104474, 127.05428913776542),
        listOf(37.56343044104474, 127.05768497333906),
        listOf(37.56343044104474, 127.06108080891270),
        listOf(37.56343044104474, 127.06447664448635),
        listOf(37.56343044104474, 127.06787248005999),
        listOf(37.56343044104474, 127.07126831563363),
        listOf(37.56343044104474, 127.07466415120727),
        listOf(37.56343044104474, 127.07805998678091),
        listOf(37.56343044104474, 127.08145582235455)
    )

    private lateinit var mapView: MapView
    private lateinit var addressThread: AddressThread
    private lateinit var binding: ActivityGuardMapBinding

    companion object {
        lateinit var naverMap: NaverMap
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardMapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        LocalBroadcastManager.getInstance(this).registerReceiver(locReceiver(), IntentFilter(App.CNG_LOC))

        sLat = App.prefs.saveLat.toDouble()
        sLng = App.prefs.saveLng.toDouble()
    }

    override fun onMapReady(naverMap: NaverMap) {                                           //지도 최초 생성
        GuardMapActivity.naverMap = naverMap
        naverMap.setMapType(NaverMap.MapType.Basic);                                        //지도 뒷 배경
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)          //건물 표시

        val cameraPosition = CameraPosition(
            LatLng(sLat, sLng),                                                           //좌표
            15.0                                                                      //줌 레벨
        )
        GuardMapActivity.naverMap.cameraPosition = cameraPosition
        changePosition(sLat, sLng)                                                        //마커 위치 변경, 주소 변경
    }

    private fun setMarker(lat: Double, lng: Double){                                        //마커 생성
        marker.position = LatLng(lat, lng)  //마커 위치
        marker.icon = OverlayImage.fromResource(R.drawable.ic_baseline_place_24)    //마커 아이콘
        marker.width = 75
        marker.height = 75
        marker.alpha = 0.8f                                                         //마커 투명도
        marker.zIndex = 10                                                          //마커 우선순위
        marker.map = naverMap
    }

    private fun changePosition(latitude: Double, longitude: Double) {                               //마커, 주소 최신화
        setMarker(latitude, longitude)
        val cameraPosition = CameraPosition(
            LatLng(latitude, longitude),
            naverMap.cameraPosition.zoom
        )
        sLat = App.prefs.saveLat.toDouble()
        sLng = App.prefs.saveLng.toDouble()

        addressThread = AddressThread()
        addressThread.run()

        naverMap.cameraPosition = cameraPosition

        drawPath(Pair(latitude, longitude))
        changeLocationText()
    }

    private fun drawPath(latLng: Pair<Double, Double>){
        if(App.prefs.isPred){
            arrowheadPath.isVisible = true
            val nextLatLng = CENTER_LAT_LNG[App.prefs.nextCell-1]
            arrowheadPath.coords = listOf(
                LatLng(latLng.first, latLng.second),
                LatLng(nextLatLng[0], nextLatLng[1])
            )
            arrowheadPath.map = naverMap
        }else{
            arrowheadPath.isVisible = false
        }
    }

    private fun changeLocationText(){
        if(prevAdd != nowAdd){
            binding.locationTxt.text = nowAdd
            prevAdd = nowAdd
        }
    }

    inner class AddressThread(): Thread(){
        override fun run() {
            locationToText(context)
        }
    }

    inner class locReceiver: BroadcastReceiver(){                                           //브로드 캐스트 리시버
        override fun onReceive(context: Context?, intent: Intent?) {
            val latitude = intent?.getDoubleExtra("latitude", App.prefs.saveLat.toDouble())
            val longitude = intent?.getDoubleExtra("longitude", App.prefs.saveLng.toDouble())
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