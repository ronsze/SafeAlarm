package com.myproject.safealarm.feature.guard.alarm

import com.myproject.safealarm.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.sdbk.domain.model.guard.Alarm

class AlarmViewModel: BaseViewModel() {
    private val _uiState: MutableStateFlow<AlarmUiState> = MutableStateFlow(AlarmUiState.Loading)
    val uiState get() = _uiState.asStateFlow()

    private val _alarmList: MutableStateFlow<List<Alarm>> = MutableStateFlow(listOf())
    val alarmList get() = _alarmList.asStateFlow()

    fun loadData() {

    }

    sealed interface AlarmUiState {
        data object Loading: AlarmUiState
        data class Loaded(val data: Int): AlarmUiState
    }
}