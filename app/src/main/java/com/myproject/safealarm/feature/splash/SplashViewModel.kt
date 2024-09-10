package com.myproject.safealarm.feature.splash

import androidx.lifecycle.viewModelScope
import com.myproject.safealarm.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.sdbk.domain.model.user.UserProfile
import kr.sdbk.domain.model.user.UserRole
import kr.sdbk.domain.usecase.user_auth.GetUserUseCase
import kr.sdbk.domain.usecase.user_service.GetUserProfileUseCase
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
): BaseViewModel() {
    private val _uiState: MutableStateFlow<SplashUiState> = MutableStateFlow(SplashUiState.Loading)
    val uiState get() = _uiState.asStateFlow()

    fun checkUser() {
        getUserUseCase(
            scope = viewModelScope,
            onSuccess = { user ->
                if (user == null) _uiState.set(SplashUiState.LoggedOut)
                else checkUserState()
            },
            onFailure = {
                basicErrorHandling(it)
            }
        )
    }

    private fun checkUserState() {
        getUserProfileUseCase(
            scope = viewModelScope,
            onSuccess = { profile ->
                checkConnection(profile)
            },
            onFailure = {
                basicErrorHandling(it)
            }
        )
    }

    private fun checkConnection(
        profile: UserProfile
    ) {
        when {
            profile.partnerId != null -> _uiState.set(SplashUiState.Connected(profile.role!!))
            else -> _uiState.set(SplashUiState.RoleSelect)
        }
    }

    sealed interface SplashUiState {
        data object Loading: SplashUiState
        data class Connected(val role: UserRole): SplashUiState
        data object RoleSelect: SplashUiState
        data object LoggedOut: SplashUiState
    }
}