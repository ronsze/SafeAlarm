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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

class GuardMapActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var binding: ActivityGuardMapBinding
    private lateinit var mapView: MapView

    private var nowAdd = "" ; private var prevAdd = ""
    private var s_lat = App.prefs.s_lat.toDouble()
    private var s_lng = App.prefs.s_lng.toDouble()
    private val marker = Marker()
    private lateinit var addThread: addressThread
    private val center_latLng: List<List<Double>> = listOf(
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

        s_lat = App.prefs.s_lat.toDouble()
        s_lng = App.prefs.s_lng.toDouble()

        addThread = addressThread()
        addThread.run()

        naverMap.cameraPosition = cameraPosition
        isPred = App.prefs.isPred
        if(isPred){
            arrowheadPath.isVisible = true
            next_cell = App.prefs.next
            drawPath(Pair(latitude, longitude), next_cell)
        }else{
            arrowheadPath.isVisible = false
        }
        if(prevAdd != nowAdd){
            binding.locationTxt.text = nowAdd
            prevAdd = nowAdd
        }
    }

    private fun drawPath(latLng: Pair<Double, Double>, next_cell: Int){
        var next_latLng = center_latLng[next_cell-1]
        arrowheadPath.coords = listOf(
            LatLng(latLng.first, latLng.second),
            LatLng(next_latLng[0], next_latLng[1])
        )
        arrowheadPath.map = naverMap
    }

    private fun cngLocation(){           //위도, 경도를 주소로 변경
        var lat_s = s_lat.toString() ; var lng_s = s_lng.toString()
        var lat = lat_s.toDouble()
        var lng = lng_s.toDouble()
        if(lat_s.length >= 9){
            lat = lat_s.substring(0, 8).toDouble()
        }
        if(lng_s.length >= 10){
            lng = lng_s.substring(0, 9).toDouble()
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
        if(mResultList != null && mResultList.size > 0){
            currentLocation = mResultList[0].getAddressLine(0)
            currentLocation = currentLocation.substring(5)
            nowAdd = currentLocation
        }
    }

    inner class addressThread(): Thread(){
        override fun run() {
            cngLocation()
        }
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