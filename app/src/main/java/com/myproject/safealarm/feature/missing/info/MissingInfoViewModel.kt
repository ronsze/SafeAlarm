package com.myproject.safealarm.feature.missing.info

import androidx.lifecycle.viewModelScope
import com.myproject.safealarm.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.sdbk.domain.model.ward.MissingInfo
import kr.sdbk.domain.usecase.user_service.GetMissingListUseCase
import javax.inject.Inject

@HiltViewModel
class MissingInfoViewModel @Inject constructor(
    private val getMissingListUseCase: GetMissingListUseCase
): BaseViewModel() {
    private val _missingList: MutableStateFlow<List<MissingInfo>> = MutableStateFlow(listOf())
    val missingList get() = _missingList.asStateFlow()

    fun loadData() {
        getMissingListUseCase(
            scope = viewModelScope,
            onSuccess = {
                _missingList.set(it)
            },
            onFailure = {

            }
        )
    }
}