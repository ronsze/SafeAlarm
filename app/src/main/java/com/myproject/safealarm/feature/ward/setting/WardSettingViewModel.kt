package com.myproject.safealarm.feature.ward.setting

import com.myproject.safealarm.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kr.sdbk.domain.model.BasicDialogState

class WardSettingViewModel: BaseViewModel() {
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