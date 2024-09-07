package com.myproject.safealarm.feature.guard.map

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.myproject.safealarm.base.BaseFragment

class MapFragment: BaseFragment<MapViewModel>() {
    override val fragmentViewModel: MapViewModel by viewModels()

    @Composable
    override fun Root() {

    }
}