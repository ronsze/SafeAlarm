package com.myproject.safealarm.feature.guard.help

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.myproject.safealarm.base.BaseFragment

class HelpFragment: BaseFragment<HelpViewModel>() {
    override val fragmentViewModel: HelpViewModel by viewModels()

    @Composable
    override fun Root() {

    }
}