package com.myproject.safealarm.feature.splash

import android.Manifest
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
import dagger.hilt.android.AndroidEntryPoint
import kr.sdbk.domain.model.user.UserRole
import kotlin.system.exitProcess

@AndroidEntryPoint
class SplashFragment : BaseFragment<SplashViewModel>() {
    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.POST_NOTIFICATIONS
        )
    }
    override val fragmentViewModel: SplashViewModel by viewModels()

    @Composable
    override fun Root() {
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(key1 = lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_CREATE) checkPermission()
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        val uiState = fragmentViewModel.uiState.collectAsStateWithLifecycle().value
        when (uiState) {
            SplashViewModel.SplashUiState.Loading -> Unit
            is SplashViewModel.SplashUiState.Connected-> navigateToMain(uiState.role)
            SplashViewModel.SplashUiState.RoleSelect -> navigateToRoleSelect()
            SplashViewModel.SplashUiState.LoggedOut -> navigateToOnboarding()
        }

        View()
    }

    @Composable
    private fun View() {
        Box {
            Image(
                painter = painterResource(id = R.drawable.load_img),
                contentDescription = "",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 25.dp)
                    .align(Alignment.Center)
            )

            BaseText(
                text = "SafeMe",
                fontSize = 70.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 35.dp)
            )
        }
    }

    private val permissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isDenied = permissions.filter { !it.value }
        if (isDenied.isEmpty()) {
            fragmentViewModel.checkUser()
        } else {
            Log.e("qweqwe", "${isDenied}")
            toast(getString(R.string.permissions_denied))
            exitProcess(0)
        }
    }

    private fun checkPermission() {
        permissionRequestLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun navigateToMain(role: UserRole) = when (role) {
        UserRole.GUARD -> navigateTo(SplashFragmentDirections.actionSplashFragmentToGuardNav())
        UserRole.WARD -> navigateTo(SplashFragmentDirections.actionSplashFragmentToWardNav())
    }
    private fun navigateToOnboarding() = navigateTo(SplashFragmentDirections.actionSplashFragmentToSignNav())
    private fun navigateToRoleSelect() = navigateTo(SplashFragmentDirections.actionSplashFragmentToRegisterNav())

    @Preview
    @Composable
    private fun Preview() {
        View()
    }
}