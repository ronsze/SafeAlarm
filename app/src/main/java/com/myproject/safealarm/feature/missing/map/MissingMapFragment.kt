package com.myproject.safealarm.feature.missing.map

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.myproject.safealarm.base.BaseFragment

class MissingMapFragment: BaseFragment<MissingMapViewModel>() {
    override val fragmentViewModel: MissingMapViewModel by viewModels()

    @Composable
    override fun Root() {

    }
}