package com.myproject.safealarm.feature.role.guard

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.myproject.safealarm.base.BaseFragment

class GuardRegisterFragment: BaseFragment<GuardRegisterViewModel>() {
    override val fragmentViewModel: GuardRegisterViewModel by viewModels()

    @Composable
    override fun Root() {

    }
}