package com.myproject.safealarm.feature.missing.each_info

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.myproject.safealarm.base.BaseFragment

class EachInfoFragment: BaseFragment<EachInfoViewModel>() {
    override val fragmentViewModel: EachInfoViewModel by viewModels()

    @Composable
    override fun Root() {

    }

    private fun navigateToMissingMap() = navigateTo(EachInfoFragmentDirections.actionEachInfoFragmentToMissingMapFragment())
}