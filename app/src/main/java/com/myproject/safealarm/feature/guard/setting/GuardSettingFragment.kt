package com.myproject.safealarm.feature.guard.setting

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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GuardSettingFragment: BaseFragment<GuardSettingViewModel>() {
    override val fragmentViewModel: GuardSettingViewModel by viewModels()

    @Composable
    override fun Root() {
        val uiState by fragmentViewModel.uiState.collectAsStateWithLifecycle()
        when (uiState) {
            GuardSettingViewModel.GuardSettingUiState.View -> Unit
            GuardSettingViewModel.GuardSettingUiState.Loading -> LoadingView()
        }

        View()
    }

    @Composable
    private fun View() {
        var isDeleteDialogVisible by remember { mutableStateOf(false) }
        if (isDeleteDialogVisible) {
            DeleteWardInfoDialog(
                onConfirm = fragmentViewModel::deleteInfo,
                onDismissRequest = { isDeleteDialogVisible = false }
            )
        }
        Column {
            BaseToolbar(
                frontComposable = BaseToolbarDefaults.defaultToolbarPainter(
                    onClick = this@GuardSettingFragment::popupBackStack
                ),
                titleComposable = BaseToolbarDefaults.defaultTitle(
                    title = stringResource(id = R.string.setting)
                )
            )

            MenuItem(
                label = stringResource(id = R.string.register_ward_info),
                onClick = this@GuardSettingFragment::navigateToWardInfo
            )

            MenuItem(
                label = stringResource(id = R.string.delete_ward_info),
                onClick = { isDeleteDialogVisible = true }
            )
        }
    }

    @Composable
    private fun MenuItem(
        label: String,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .padding(horizontal = 15.dp)
                .clickable { onClick() }
        ) {
            BaseText(
                text = label,
                fontSize = 16.sp,
            )
        }
    }

    private fun navigateToWardInfo() = navigateTo(GuardSettingFragmentDirections.actionGuardSettingFragmentToWardInfoFragment())
}