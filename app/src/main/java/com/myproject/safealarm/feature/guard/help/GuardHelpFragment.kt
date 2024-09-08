package com.myproject.safealarm.feature.guard.help

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myproject.safealarm.R
import com.myproject.safealarm.base.BaseFragment
import com.myproject.safealarm.ui.composable.BaseText
import com.myproject.safealarm.ui.composable.BaseToolbar
import com.myproject.safealarm.ui.composable.BaseToolbarDefaults
import com.myproject.safealarm.ui.composable.BasicButton
import com.myproject.safealarm.ui.composable.HintTextField
import com.myproject.safealarm.util.locationToText
import kotlinx.coroutines.launch

class GuardHelpFragment: BaseFragment<GuardHelpViewModel>() {
    override val fragmentViewModel: GuardHelpViewModel by viewModels()

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

        val uiState by fragmentViewModel.uiState.collectAsStateWithLifecycle()
        when (uiState) {
            GuardHelpViewModel.GuardHelpUiState.Loading -> {
                LoadingView()
            }
            GuardHelpViewModel.GuardHelpUiState.WardInfoLoaded -> {
                Unit
            }
            GuardHelpViewModel.GuardHelpUiState.WardInfoLoadFailed -> {
                toast(stringResource(id = R.string.ward_info_load_failed))
                popupBackStack()
            }
            GuardHelpViewModel.GuardHelpUiState.PostSuccess -> {
                toast(stringResource(id = R.string.registered))
                popupBackStack()
            }
            GuardHelpViewModel.GuardHelpUiState.PostFailed -> {
                toast("error")
                popupBackStack()
            }
        }

        View()
    }

    @Composable
    private fun View() {
        Column {
            val scope = rememberCoroutineScope()

            BaseToolbar(
                frontComposable = BaseToolbarDefaults.defaultToolbarPainter(
                    onClick = this@GuardHelpFragment::popupBackStack
                ),
                titleComposable = BaseToolbarDefaults.defaultTitle(
                    title = stringResource(id = R.string.post_missing)
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            val missingTime = remember { mutableStateOf("") }
            var lastLocation by remember { mutableStateOf("") }
            val signalment = remember { mutableStateOf("") }
            val extra = remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                scope.launch {
                    lastLocation = locationToText(requireContext(), fragmentViewModel.getLastWardLocation())
                }
            }

            HintTextField(
                hint = stringResource(id = R.string.missing_time),
                text = missingTime,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            LastLocationLayer(
                lastLocation = lastLocation
            )
            Spacer(modifier = Modifier.height(10.dp))

            HintTextField(
                hint = stringResource(id = R.string.signalment),
                text = signalment,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.height(10.dp))

            HintTextField(
                hint = stringResource(id = R.string.extra),
                text = extra,
                fontSize = 16.sp,
                modifier = Modifier.weight(2f)
            )
            Spacer(modifier = Modifier.height(20.dp))

            BasicButton(
                text = stringResource(id = R.string.confirm),
                onClick = {
                    fragmentViewModel.postMissing(
                        missingTime = missingTime.value,
                        lastLocation = lastLocation,
                        signalment = signalment.value,
                        extra = extra.value
                    )
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    @Composable
    private fun LastLocationLayer(
        lastLocation: String
    ) {
        BaseText(
            text = stringResource(id = R.string.last_location),
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(3.dp))

        BaseText(
            text = lastLocation,
            fontSize = 14.sp
        )
    }
}