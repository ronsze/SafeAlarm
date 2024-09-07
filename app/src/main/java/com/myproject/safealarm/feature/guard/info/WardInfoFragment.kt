package com.myproject.safealarm.feature.guard.info

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.myproject.safealarm.base.BaseFragment

class WardInfoFragment: BaseFragment<WardInfoViewModel>() {
    override val fragmentViewModel: WardInfoViewModel by viewModels()

    @Composable
    override fun Root() {

    }
}