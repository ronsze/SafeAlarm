package com.myproject.safealarm.feature.missing.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.myproject.safealarm.R
import com.myproject.safealarm.base.BaseFragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage

class MissingMapFragment: BaseFragment<MissingMapViewModel>(), OnMapReadyCallback {
    override val fragmentViewModel: MissingMapViewModel by viewModels()
    private val args: MissingMapFragmentArgs by navArgs()
    private lateinit var naverMap: NaverMap
    private lateinit var mapView: MapView

    @Composable
    override fun Root() {
        AndroidView(factory = {
            mapView = MapView(it).apply {
                getMapAsync(this@MissingMapFragment)
            }
            mapView
        })
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        naverMap.mapType = NaverMap.MapType.Basic
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)

        val lastLocation = LatLng(args.lastLocation.latitude, args.lastLocation.longitude)

        Marker().apply {    //ward marker
            position = lastLocation
            this.map = naverMap
            icon = OverlayImage.fromResource(R.drawable.ward_img)
            alpha = 0.8f
            zIndex = 10
            width = 100
            height = 100
        }

        naverMap.moveCamera(lastLocation)
    }
}