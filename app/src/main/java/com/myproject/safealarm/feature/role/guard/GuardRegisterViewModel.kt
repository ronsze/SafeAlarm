package com.myproject.safealarm.feature.role.guard

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
class GuardRegisterViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
): BaseViewModel() {
    private val _uiState: MutableStateFlow<GuardRegisterUiState> = MutableStateFlow(GuardRegisterUiState.QRScanning)
    val uiState get() = _uiState.asStateFlow()

    fun connect(wardUid: String) {
        _uiState.set(GuardRegisterUiState.Loading)
        getUserUseCase(
            scope = viewModelScope,
            onSuccess = { user ->
                user?.uid?.run {
                    updateProfile(
                        uid = this,
                        wardUid = wardUid
                    )
                }
            },
            onFailure = {

            }
        )
    }

    private fun updateProfile(uid: String, wardUid: String) {
        val profile = UserProfile(
            uid = uid,
            partnerId = wardUid,
            role = UserRole.GUARD
        )
        updateUserProfileUseCase(
            profile = profile,
            scope = viewModelScope,
            onSuccess = {
                _uiState.set(GuardRegisterUiState.Connected)
            },
            onFailure = {

            }
        )
    }

    sealed interface GuardRegisterUiState {
        data object QRScanning: GuardRegisterUiState
        data object Loading: GuardRegisterUiState
        data object Failed: GuardRegisterUiState
        data object Connected: GuardRegisterUiState
    }
}