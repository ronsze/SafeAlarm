package com.myproject.safealarm.feature.sign.sign_up

import com.myproject.safealarm.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignUpViewModel: BaseViewModel() {
    private val _emailError: MutableStateFlow<EmailError> = MutableStateFlow(EmailError.None)
    val emailError get() = _emailError.asStateFlow()

    private val _confirmPasswordError: MutableStateFlow<PasswordError> = MutableStateFlow(PasswordError.None)
    val confirmPasswordError get() = _confirmPasswordError.asStateFlow()

    fun signUp(
        email: String,
        password: String,
        confirmPassword: String
    ) {
        when {
            password != confirmPassword -> _confirmPasswordError.set(PasswordError.PasswordNotMatched)
        }
    }

    fun resetEmailError() { if (emailError.value != EmailError.None) _emailError.set(EmailError.None) }
    fun resetConfirmPasswordError() { if (confirmPasswordError.value != PasswordError.None) _confirmPasswordError.set(PasswordError.None) }

    sealed interface EmailError {
        data object None: EmailError
        data object ExistsEmail: EmailError
    }

    sealed interface PasswordError {
        data object None: PasswordError
        data object PasswordNotMatched: PasswordError
    }
}