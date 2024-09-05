package com.myproject.safealarm.feature.sign.sign_up

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.myproject.safealarm.base.BaseFragment

class SignUpFragment: BaseFragment<SignUpViewModel>() {
    override val fragmentViewModel: SignUpViewModel by viewModels()

    @Composable
    override fun Root() {

    }
}