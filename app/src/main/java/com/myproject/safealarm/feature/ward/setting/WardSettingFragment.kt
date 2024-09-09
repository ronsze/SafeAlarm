package com.myproject.safealarm.feature.ward.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myproject.safealarm.R
import com.myproject.safealarm.base.BaseFragment
import com.myproject.safealarm.ui.composable.BaseText
import com.myproject.safealarm.ui.composable.BaseToolbar
import com.myproject.safealarm.ui.composable.BaseToolbarDefaults
import kr.sdbk.domain.model.BasicDialogState

class WardSettingFragment: BaseFragment<WardSettingViewModel>() {
    override val fragmentViewModel: WardSettingViewModel by viewModels()

    @Composable
    override fun Root() {
        var isSetPasswordDialogVisible by remember { mutableStateOf(false) }
        val verifyDialogState by fragmentViewModel.verifyDialogState.collectAsStateWithLifecycle()

        if (isSetPasswordDialogVisible) {
            SetPasswordDialog(
                onConfirm = fragmentViewModel::changeVerificationPassword,
                onDismissRequest = { isSetPasswordDialogVisible = false }
            )
        }

        if (verifyDialogState.isVisible) {
            VerifyDialog(
                onConfirm = fragmentViewModel::verifyAlarm,
                onDismissRequest = { fragmentViewModel.verifyDialogState.value = BasicDialogState() }
            )
        }
        Column {
            BaseToolbar(
                frontComposable = BaseToolbarDefaults.defaultToolbarPainter(
                    onClick = this@WardSettingFragment::popupBackStack
                ),
                titleComposable = BaseToolbarDefaults.defaultTitle(
                    title = stringResource(id = R.string.setting)
                )
            )

            MenuItem(
                text = stringResource(id = R.string.change_verification_password),
                onClick = { isSetPasswordDialogVisible = true }
            )

            MenuItem(
                text = stringResource(id = R.string.verify_alarm),
                onClick = fragmentViewModel::checkVerification
            )
        }
    }

    @Composable
    private fun MenuItem(
        text: String,
        onClick: () -> Unit
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .padding(horizontal = 15.dp)
                .clickable { onClick() }
        ) {
            BaseText(
                text = text,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}