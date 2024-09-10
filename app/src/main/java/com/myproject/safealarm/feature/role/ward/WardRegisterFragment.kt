package com.myproject.safealarm.feature.role.ward

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.myproject.safealarm.R
import com.myproject.safealarm.base.BaseFragment
import com.myproject.safealarm.ui.composable.BaseText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WardRegisterFragment: BaseFragment<WardRegisterViewModel>() {
    override val fragmentViewModel: WardRegisterViewModel by viewModels()

    @Composable
    override fun Root() {
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(key1 = lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_CREATE) fragmentViewModel.connect()
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        val uiState = fragmentViewModel.uiState.collectAsStateWithLifecycle().value
        when (uiState) {
            WardRegisterViewModel.WardRegisterUiState.Loading -> LoadingView()
            is WardRegisterViewModel.WardRegisterUiState.Loaded -> View(uid = uiState.uid)
            WardRegisterViewModel.WardRegisterUiState.Failed -> Unit
            WardRegisterViewModel.WardRegisterUiState.Connected -> Unit
        }
    }

    @Composable
    private fun View(
        uid: String
    ) {
        val appName = stringResource(id = R.string.app_name)
        val qrCodeBitmap by remember {
            mutableStateOf(
                BarcodeEncoder().encodeBitmap(
                    "$appName $uid",
                    BarcodeFormat.QR_CODE,
                    400,
                    400
                )
            )
        }

        Box {
            Image(
                bitmap = qrCodeBitmap.asImageBitmap(),
                contentDescription = "",
                modifier = Modifier
                    .size(400.dp)
                    .align(Alignment.Center)
            )
            
            BaseText(
                text = stringResource(id = R.string.please_scan_qrcode),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}