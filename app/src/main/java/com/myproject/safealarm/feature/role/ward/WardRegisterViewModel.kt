package com.myproject.safealarm.feature.role.ward

import androidx.lifecycle.viewModelScope
import com.myproject.safealarm.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.sdbk.domain.model.user.UserProfile
import kr.sdbk.domain.model.user.UserRole
import kr.sdbk.domain.usecase.user_auth.GetUserUseCase
import kr.sdbk.domain.usecase.user_service.UpdateUserProfileUseCase
import javax.inject.Inject


@HiltViewModel
class WardRegisterViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
): BaseViewModel() {
    private val _uiState: MutableStateFlow<WardRegisterUiState> = MutableStateFlow(WardRegisterUiState.Loading)
    val uiState get() = _uiState.asStateFlow()

    fun loadData() {
        getUserUseCase(
            scope = viewModelScope,
            onSuccess = { user ->
                user?.run {
                    _uiState.set(WardRegisterUiState.Loaded(uid))
                    updateProfile(uid)
                } ?: _uiState.set(WardRegisterUiState.Failed)
            },
            onFailure = {
                _uiState.set(WardRegisterUiState.Failed)
                basicErrorHandling(it)
            }
        )
    }

    private fun updateProfile(uid: String) {
        val profile = UserProfile(
            uid = uid,
            partnerId = null,
            role = UserRole.WARD
        )
        updateUserProfileUseCase(
            profile = profile,
            scope = viewModelScope,
            onSuccess = {
                _uiState.set(WardRegisterUiState.Connected)
            },
            onFailure = {}
        )
    }

    sealed interface WardRegisterUiState {
        data object Loading: WardRegisterUiState
        data class Loaded(val uid: String): WardRegisterUiState
        data object Failed: WardRegisterUiState
        data object Connected: WardRegisterUiState
    }
}