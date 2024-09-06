package com.myproject.safealarm.feature.sign.sign_up

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.myproject.safealarm.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.sdbk.domain.usecase.user_auth.SignUpUseCase
import javax.annotation.Signed
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
): BaseViewModel() {
    private val _uiState: MutableStateFlow<SignUpUiState> = MutableStateFlow(SignUpUiState.View)
    val uiState get() = _uiState.asStateFlow()

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
            password != confirmPassword -> {
                _confirmPasswordError.set(PasswordError.PasswordNotMatched)
            }
            else -> {
                signUpUseCase(
                    email = email,
                    password = password,
                    scope = viewModelScope,
                    onSuccess = {
                        _uiState.set(SignUpUiState.Signed)
                    },
                    onFailure = {
                        when (it) {
                            is FirebaseAuthUserCollisionException -> _emailError.set(EmailError.ExistsEmail)
                            else -> basicErrorHandling(it)
                        }
                    }
                )
            }
        }
    }

    fun resetEmailError() { if (emailError.value != EmailError.None) _emailError.set(EmailError.None) }
    fun resetConfirmPasswordError() { if (confirmPasswordError.value != PasswordError.None) _confirmPasswordError.set(PasswordError.None) }

    sealed interface SignUpUiState {
        data object View: SignUpUiState
        data object Signed: SignUpUiState
    }

    sealed interface EmailError {
        data object None: EmailError
        data object ExistsEmail: EmailError
    }

    sealed interface PasswordError {
        data object None: PasswordError
        data object PasswordNotMatched: PasswordError
    }
}