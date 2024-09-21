package com.myproject.safealarm.feature.guard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myproject.safealarm.R
import com.myproject.safealarm.base.BaseFragment
import com.myproject.safealarm.ui.composable.MenuCard

class GuardFragment: BaseFragment<GuardViewModel>() {
    override val fragmentViewModel: GuardViewModel by viewModels()

    @Composable
    override fun Root() {
        val lifecycleOwner = LocalLifecycleOwner.current
        val uiState by fragmentViewModel.uiState.collectAsStateWithLifecycle()

        DisposableEffect(key1 = lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_CREATE && uiState != GuardViewModel.GuardUiState.Connected) fragmentViewModel.connect()
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        when (uiState) {
            GuardViewModel.GuardUiState.Loading -> LoadingView()
            GuardViewModel.GuardUiState.Connected -> View()
        }
    }

    @Composable
    private fun View() {
        LazyVerticalGrid(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .padding(horizontal = 20.dp)
        ) {
            item {
                MenuCard(
                    image = R.drawable.map_img,
                    label = R.string.check_location,
                    onClick = this@GuardFragment::navigateToMap
                )
            }

            item {
                MenuCard(
                    image = R.drawable.missing_img,
                    label = R.string.missing_register,
                    onClick = this@GuardFragment::navigateToHelp
                )
            }

            item {
                MenuCard(
                    image = R.drawable.list_img,
                    label = R.string.missing_info,
                    onClick = this@GuardFragment::navigateToMissingInfo
                )
            }

            item {
                MenuCard(
                    image = R.drawable.ic_alarm,
                    label = R.string.alarm_register,
                    onClick = this@GuardFragment::navigateToAlarm
                )
            }

            item {
                MenuCard(
                    image = R.drawable.set_img,
                    label = R.string.setting,
                    onClick = this@GuardFragment::navigateToSetting
                )
            }
        }
    }

    private fun navigateToMap() = navigateTo(GuardFragmentDirections.actionGuardFragmentToMapFragment())
    private fun navigateToHelp() = navigateTo(GuardFragmentDirections.actionGuardFragmentToHelpFragment())
    private fun navigateToMissingInfo() = navigateTo(GuardFragmentDirections.actionGuardFragmentToMissingNav())
    private fun navigateToAlarm() = navigateTo(GuardFragmentDirections.actionGuardFragmentToAlarmFragment())
    private fun navigateToSetting() = navigateTo(GuardFragmentDirections.actionGuardFragmentToGuardSetting())

    @Preview
    @Composable
    private fun Preview() {
        View()
    }
}