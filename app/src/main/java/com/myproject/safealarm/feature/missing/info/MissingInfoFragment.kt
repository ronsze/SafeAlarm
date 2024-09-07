package com.myproject.safealarm.feature.missing.info

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.myproject.safealarm.base.BaseFragment

class MissingInfoFragment: BaseFragment<MissingInfoViewModel>() {
    override val fragmentViewModel: MissingInfoViewModel by viewModels()

    @Composable
    override fun Root() {

    }

    private fun navigateToEachInfo() = navigateTo(MissingInfoFragmentDirections.actionMissingInfoFragmentToEachInfoFragment())
}