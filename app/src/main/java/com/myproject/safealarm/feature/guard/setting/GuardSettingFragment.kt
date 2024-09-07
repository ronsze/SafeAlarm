package com.myproject.safealarm.feature.guard.setting

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.myproject.safealarm.base.BaseFragment

class GuardSettingFragment: BaseFragment<GuardSettingViewModel>() {
    override val fragmentViewModel: GuardSettingViewModel by viewModels()

    @Composable
    override fun Root() {

    }

    private fun navigateToWardInfo() = navigateTo(GuardSettingFragmentDirections.actionGuardSettingFragmentToWardInfoFragment())
}