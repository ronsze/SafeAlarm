package com.myproject.safealarm.feature.ward

import android.content.pm.ServiceInfo
import android.os.Build
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
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myproject.safealarm.R
import com.myproject.safealarm.base.BaseFragment
import com.myproject.safealarm.service.WardLocationService
import com.myproject.safealarm.ui.composable.MenuCard

class WardFragment: BaseFragment<WardViewModel>() {
    override val fragmentViewModel: WardViewModel by viewModels()

    @Composable
    override fun Root() {
        val lifecycleOwner = LocalLifecycleOwner.current
        val uiState by fragmentViewModel.uiState.collectAsStateWithLifecycle()

        DisposableEffect(key1 = lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_CREATE && uiState != WardViewModel.WardUiState.Connected) fragmentViewModel.connect()
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        when (uiState) {
            WardViewModel.WardUiState.Loading -> LoadingView()
            WardViewModel.WardUiState.Connected -> View()
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
                    image = R.drawable.help_img,
                    label = R.string.help,
                    onClick = this@WardFragment::help
                )
            }

            item {
                MenuCard(
                    image = R.drawable.missing_img,
                    label = R.string.missing_register,
                    onClick = this@WardFragment::navigateToMissingInfo
                )
            }

            item {
                MenuCard(
                    image = R.drawable.set_img,
                    label = R.string.setting,
                    onClick = this@WardFragment::navigateToSetting
                )
            }
        }
    }

    private fun startLocationService() {
        val noti = NotificationCompat.Builder(requireContext(), "CH").build()
        ServiceCompat.startForeground(
            WardLocationService(),
            100,
            noti,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            else 0
        )
    }

    private fun help() {
//        val intent = Intent(this, ForegroundService::class.java)
//        intent.action = Actions.HELP_CALL_WARD
//        startService(intent)
    }

    private fun navigateToMissingInfo() = navigateTo(WardFragmentDirections.actionWardFragmentToMissingNav())
    private fun navigateToSetting() = navigateTo(WardFragmentDirections.actionWardFragmentToWardSettingFragment())

    @Preview
    @Composable
    private fun Preview() {
        View()
    }
}