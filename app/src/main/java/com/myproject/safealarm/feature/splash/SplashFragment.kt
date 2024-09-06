package com.myproject.safealarm.feature.splash

import android.Manifest
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myproject.safealarm.R
import com.myproject.safealarm.base.BaseFragment
import com.myproject.safealarm.ui.composable.BaseText
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess

@AndroidEntryPoint
class SplashFragment : BaseFragment<SplashViewModel>() {
    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    override val fragmentViewModel: SplashViewModel by viewModels()

    @Composable
    override fun Root() {
        val uiState by fragmentViewModel.uiState.collectAsStateWithLifecycle()
        when (uiState) {
            SplashViewModel.SplashUiState.Loading -> checkPermission()
            SplashViewModel.SplashUiState.Connected-> navigateToMain()
            SplashViewModel.SplashUiState.RoleSelect -> navigateToRoleSelect()
            SplashViewModel.SplashUiState.LoggedOut -> navigateToOnboarding()
        }

        View()
    }

    @Composable
    private fun View() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.load_img),
                contentDescription = "",
                modifier = Modifier
                    .fillMaxWidth()
            )

            BaseText(
                text = "SafeMe",
                fontSize = 70.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    private val permissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.all { it.value }
        if (isGranted) {
            fragmentViewModel.checkUser()
        } else {
            Toast.makeText(requireContext(), "권한 설정이 거부되었습니다.\n앱을 사용하시려면 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
            exitProcess(0)
        }
    }

    private fun checkPermission() {
        permissionRequestLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun navigateToMain() = navigateTo(SplashFragmentDirections.actionSplashFragmentToSignNav())
    private fun navigateToOnboarding() = navigateTo(SplashFragmentDirections.actionSplashFragmentToSignNav())
    private fun navigateToRoleSelect() = navigateTo(SplashFragmentDirections.actionSplashFragmentToRegisterNav())

    @Preview
    @Composable
    private fun Preview() {
        View()
    }
}