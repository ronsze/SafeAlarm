package com.myproject.safealarm.feature.ward.setting

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.myproject.safealarm.base.BaseFragment

class WardSettingFragment: BaseFragment<WardSettingViewModel>() {
    override val fragmentViewModel: WardSettingViewModel by viewModels()

    @Composable
    override fun Root() {

    }
}