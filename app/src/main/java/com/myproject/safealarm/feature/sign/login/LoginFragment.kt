package com.myproject.safealarm.feature.sign.login

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.myproject.safealarm.base.BaseFragment

class LoginFragment: BaseFragment<LoginViewModel>() {
    override val fragmentViewModel: LoginViewModel by viewModels()

    @Composable
    override fun Root() {

    }
}