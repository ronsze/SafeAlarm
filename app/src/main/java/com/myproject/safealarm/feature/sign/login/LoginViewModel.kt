package com.myproject.safealarm.feature.sign.login

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.myproject.safealarm.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.sdbk.domain.usecase.user_auth.LoginUseCase
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
): BaseViewModel() {
    private val _uiState: MutableStateFlow<LoginUiState> = MutableStateFlow(LoginUiState.View)
    val uiState get() = _uiState.asStateFlow()

    private val _passwordError: MutableStateFlow<PasswordError> = MutableStateFlow(PasswordError.None)
    val passwordError get() = _passwordError.asStateFlow()

    fun login(
        email: String,
        password: String
    ) {
        loginUseCase(
            email = email,
            password = password,
            scope = viewModelScope,
            onSuccess = {
                _uiState.set(LoginUiState.LoggedIn)
            },
            onFailure = {
                when (it) {
                    is FirebaseAuthInvalidUserException,
                    is FirebaseAuthInvalidCredentialsException -> _passwordError.set(PasswordError.WrongEmailOrPassword)
                    else -> basicErrorHandling(it)
                }
            }
        )
    }

    fun resetPasswordError() { if (passwordError.value != PasswordError.None) _passwordError.set(PasswordError.None) }

    sealed interface LoginUiState {
        data object View: LoginUiState
        data object LoggedIn: LoginUiState
    }

    sealed interface PasswordError {
        data object None: PasswordError
        data object WrongEmailOrPassword: PasswordError
    }
}