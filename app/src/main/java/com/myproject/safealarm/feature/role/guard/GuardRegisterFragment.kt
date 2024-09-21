package com.myproject.safealarm.feature.role.guard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.myproject.safealarm.R
import com.myproject.safealarm.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GuardRegisterFragment: BaseFragment<GuardRegisterViewModel>() {
    override val fragmentViewModel: GuardRegisterViewModel by viewModels()

    private val scannerLauncher = registerForActivityResult(
        ScanContract()
    ) { result ->
        val appName = getString(R.string.app_name)
        if (result.contents == null) {
            onFailed(getString(R.string.cancelled))
        } else {
            val contents = result.contents.split(" ")
            if (contents[0] != appName) {
                onFailed(getString(R.string.wrong_qrcode))
            } else {
                fragmentViewModel.connect(contents[1])
            }
        }
    }

    @Composable
    override fun Root() {
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(key1 = lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_CREATE) fragmentViewModel.loadData()
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        val uiState = fragmentViewModel.uiState.collectAsStateWithLifecycle().value
        when (uiState) {
            GuardRegisterViewModel.GuardRegisterUiState.QRScanning -> scanQRCode()
            GuardRegisterViewModel.GuardRegisterUiState.Loading -> LoadingView()
            GuardRegisterViewModel.GuardRegisterUiState.Failed -> Unit
            GuardRegisterViewModel.GuardRegisterUiState.Connected -> Unit
        }
    }

    private fun scanQRCode() {
        val scanOptions = ScanOptions()
            .setBeepEnabled(false)
            .setOrientationLocked(true)
            .setPrompt(getString(R.string.please_scan_ward_qrcode))

        scannerLauncher.launch(scanOptions)
    }

    private fun onFailed(message: String) {
        toast(message)
        popupBackStack()
    }
}