package com.myproject.safealarm.feature.ward.setting

import com.myproject.safealarm.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kr.sdbk.domain.model.BasicDialogState
import javax.inject.Inject

@HiltViewModel
class WardSettingViewModel @Inject constructor(): BaseViewModel() {
    val verifyDialogState: MutableStateFlow<BasicDialogState<Nothing>> = MutableStateFlow(
        BasicDialogState()
    )

    fun checkVerification() {

    }

    fun changeVerificationPassword(
        previousPassword: String,
        newPassword: String
    ) {

    }

    fun verifyAlarm(
        password: String
    ) {

    }
}