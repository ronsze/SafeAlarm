package com.myproject.safealarm.feature.guard.alarm

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.myproject.safealarm.base.BaseFragment

class AlarmFragment: BaseFragment<AlarmViewModel>() {
    override val fragmentViewModel: AlarmViewModel by viewModels()

    @Composable
    override fun Root() {

    }
}