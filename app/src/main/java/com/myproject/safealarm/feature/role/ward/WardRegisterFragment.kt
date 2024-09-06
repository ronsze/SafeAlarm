package com.myproject.safealarm.feature.role.ward

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.myproject.safealarm.base.BaseFragment

class WardRegisterFragment: BaseFragment<WardRegisterViewModel>() {
    override val fragmentViewModel: WardRegisterViewModel by viewModels()

    @Composable
    override fun Root() {

    }
}