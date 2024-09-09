package com.myproject.safealarm.feature.guard.map

import com.myproject.safealarm.base.BaseViewModel
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(): BaseViewModel() {
    private val _wardLocation: MutableStateFlow<LatLng?> = MutableStateFlow(null)
    val wardLocation get() = _wardLocation.asStateFlow()
}