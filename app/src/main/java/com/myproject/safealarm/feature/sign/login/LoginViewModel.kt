package com.myproject.safealarm.feature.sign.login

import com.myproject.safealarm.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel: BaseViewModel() {
    private val _passwordError: MutableStateFlow<PasswordError> = MutableStateFlow(PasswordError.None)
    val passwordError get() = _passwordError.asStateFlow()

    fun login(
        email: String,
        password: String
    ) {

    }

    fun resetPasswordError() { if (passwordError.value != PasswordError.None) _passwordError.set(PasswordError.None) }

    sealed interface PasswordError {
        data object None: PasswordError
        data object WrongEmailOrPassword: PasswordError
    }
}