package com.myproject.safealarm.feature.role

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.myproject.safealarm.base.BaseFragment

class RoleSelectFragment: BaseFragment<RoleSelectViewModel>() {
    override val fragmentViewModel: RoleSelectViewModel by viewModels()

    @Composable
    override fun Root() {

    }
}