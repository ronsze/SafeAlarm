package com.myproject.safealarm.feature.guard.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.myproject.safealarm.R
import com.myproject.safealarm.base.BaseFragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.launch

class MapFragment: BaseFragment<MapViewModel>(), OnMapReadyCallback {
    override val fragmentViewModel: MapViewModel by viewModels()
    private lateinit var naverMap: NaverMap
    private lateinit var mapView: MapView
    private lateinit var wardMarker: Marker
    private var initialized: Boolean = false

    @Composable
    override fun Root() {
        AndroidView(factory = {
            mapView = MapView(it).apply {
                getMapAsync(this@MapFragment)
            }
            mapView
        })
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        naverMap.mapType = NaverMap.MapType.Basic
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true)      //건물 표시

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                fragmentViewModel.wardLocation.collect { location ->
                    location?.run {
                        if (!initialized) {
                            initialized = true
                            createWardMarker(location)
                            naverMap.moveCamera(this)
                        } else {
                            wardLocationChanged(latLng = this)
                        }
                    }
                }
            }
        }
    }

    private fun createWardMarker(latLng: LatLng) {
        wardMarker = Marker().apply {
            position = latLng
            icon = OverlayImage.fromResource(R.drawable.ward_img)
            width = 100
            height = 100
            alpha = 0.8f
            zIndex = 10
            map = naverMap
        }
    }

    private fun wardLocationChanged(latLng: LatLng) {
        wardMarker.position = latLng
    }
}