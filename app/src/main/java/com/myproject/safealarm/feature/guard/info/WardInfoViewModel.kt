package com.myproject.safealarm.feature.guard.info

import androidx.lifecycle.viewModelScope
import com.myproject.safealarm.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.sdbk.domain.model.ward.WardInfo
import kr.sdbk.domain.usecase.user_service.GetWardInfoUseCase
import kr.sdbk.domain.usecase.user_service.UpdateWardInfoUseCase
import javax.inject.Inject

@HiltViewModel
class WardInfoViewModel @Inject constructor(
    private val getWardInfoUseCase: GetWardInfoUseCase,
    private val updateWardInfoUseCase: UpdateWardInfoUseCase
): BaseViewModel() {
    private val _uiState: MutableStateFlow<WardInfoUiState> = MutableStateFlow(WardInfoUiState.Loading)
    val uiState get() = _uiState.asStateFlow()

    fun loadData() {
        getWardInfoUseCase(
            scope = viewModelScope,
            onSuccess = {
                _uiState.set(WardInfoUiState.Loaded(it))
            },
            onFailure = {
                _uiState.set(WardInfoUiState.Failed)
            }
        )
    }

    fun saveInfo(info: WardInfo) {
        _uiState.set(WardInfoUiState.Loading)
        updateWardInfoUseCase(
            wardInfo = info,
            scope = viewModelScope,
            onSuccess = {
                _uiState.set(WardInfoUiState.Loaded(it))
            },
            onFailure = {
                _uiState.set(WardInfoUiState.Failed)
            }
        )
    }

    sealed interface WardInfoUiState {
        data object Loading: WardInfoUiState
        data class Loaded(val data: WardInfo?): WardInfoUiState
        data object Failed: WardInfoUiState
    }
}