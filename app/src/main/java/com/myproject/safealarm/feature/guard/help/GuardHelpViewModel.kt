package com.myproject.safealarm.feature.guard.help

import androidx.lifecycle.viewModelScope
import com.myproject.safealarm.base.BaseViewModel
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.sdbk.domain.model.ward.MissingInfo
import kr.sdbk.domain.model.ward.WardInfo
import kr.sdbk.domain.usecase.user_service.GetWardInfoUseCase
import kr.sdbk.domain.usecase.user_service.PostMissingUseCase
import javax.inject.Inject

@HiltViewModel
class GuardHelpViewModel @Inject constructor(
    private val getWardInfoUseCase: GetWardInfoUseCase,
    private val postMissingUseCase: PostMissingUseCase
): BaseViewModel() {
    private val _uiState: MutableStateFlow<GuardHelpUiState> = MutableStateFlow(GuardHelpUiState.Loading)
    val uiState get() = _uiState.asStateFlow()

    private lateinit var wardInfo: WardInfo

    fun loadData() {
        getWardInfoUseCase(
            scope = viewModelScope,
            onSuccess = {
                wardInfo = it
                _uiState.set(GuardHelpUiState.WardInfoLoaded)
            },
            onFailure = {
                _uiState.set(GuardHelpUiState.WardInfoLoadFailed)
            }
        )
    }

    fun postMissing(
        missingTime: String,
        lastLocation: String,
        signalment: String,
        extra: String
    ) {
        _uiState.set(GuardHelpUiState.Loading)
        val missingInfo = MissingInfo(
            missingTime = missingTime,
            lastLocation = lastLocation,
            signalment = signalment,
            extra = extra,
            wardInfo = wardInfo
        )
        postMissingUseCase(
            missingInfo = missingInfo,
            scope = viewModelScope,
            onSuccess = {
                _uiState.set(GuardHelpUiState.PostSuccess)
            },
            onFailure = {
                _uiState.set(GuardHelpUiState.PostFailed)
            }
        )
    }

    fun getLastWardLocation(): LatLng {
        return LatLng(20.0, 30.0)
    }

    sealed interface GuardHelpUiState {
        data object Loading: GuardHelpUiState
        data object WardInfoLoaded: GuardHelpUiState
        data object WardInfoLoadFailed: GuardHelpUiState
        data object PostSuccess: GuardHelpUiState
        data object PostFailed: GuardHelpUiState
    }
}