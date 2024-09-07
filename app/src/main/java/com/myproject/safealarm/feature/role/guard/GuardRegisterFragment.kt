package com.myproject.safealarm.feature.role.guard

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
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
            scanFailed(getString(R.string.cancelled))
        } else {
            val contents = result.contents.split(" ")
            if (contents[0] != appName) {
                scanFailed(getString(R.string.wrong_qrcode))
            } else {
//                fragmentViewModel.startDHExchange(result.contents)
                fragmentViewModel.connect()
            }
        }
    }

    @Composable
    override fun Root() {
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

    private fun scanFailed(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        popupBackStack()
    }
}