package com.myproject.safealarm.feature.guard.setting

import androidx.lifecycle.viewModelScope
import com.myproject.safealarm.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.sdbk.domain.usecase.user_service.DeleteWardInfoUseCase
import javax.inject.Inject

@HiltViewModel
class GuardSettingViewModel @Inject constructor(
    private val deleteWardInfoUseCase: DeleteWardInfoUseCase
): BaseViewModel() {
    private val _uiState: MutableStateFlow<GuardSettingUiState> = MutableStateFlow(GuardSettingUiState.View)
    val uiState get() = _uiState.asStateFlow()

    fun deleteInfo() {
        _uiState.set(GuardSettingUiState.Loading)
        deleteWardInfoUseCase(
            scope = viewModelScope,
            onSuccess = {
                _uiState.set(GuardSettingUiState.View)
            },
            onFailure = {
                _uiState.set(GuardSettingUiState.View)
            }
        )
    }

    sealed interface GuardSettingUiState {
        data object View: GuardSettingUiState
        data object Loading: GuardSettingUiState
    }
}