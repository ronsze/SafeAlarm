package com.myproject.safealarm.feature.guard.map

import com.myproject.safealarm.base.BaseViewModel
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapViewModel: BaseViewModel() {
    private val _wardLocation: MutableStateFlow<LatLng?> = MutableStateFlow(null)
    val wardLocation get() = _wardLocation.asStateFlow()
}