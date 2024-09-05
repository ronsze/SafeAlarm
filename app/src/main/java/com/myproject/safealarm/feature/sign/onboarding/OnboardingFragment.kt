package com.myproject.safealarm.feature.sign.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import com.myproject.safealarm.R
import com.myproject.safealarm.base.BaseFragment
import com.myproject.safealarm.base.BaseViewModel
import com.myproject.safealarm.ui.composable.BaseText
import com.myproject.safealarm.ui.theme.SkyBlue

class OnboardingFragment: BaseFragment<BaseViewModel>() {
    override val fragmentViewModel: BaseViewModel by viewModels()

    @Composable
    override fun Root() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            NavigateButton(
                text = stringResource(id = R.string.sign_up),
                onClick = this@OnboardingFragment::navigateToSignUp
            )
            Spacer(modifier = Modifier.height(15.dp))

            NavigateButton(
                text = stringResource(id = R.string.login),
                onClick = this@OnboardingFragment::navigateToLogin
            )
            Spacer(modifier = Modifier.height(25.dp))
        }
    }
    
    @Composable
    private fun NavigateButton(
        text: String,
        onClick: () -> Unit
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = SkyBlue
            ),
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
        ) {
            BaseText(
                text = text,
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }

    @Preview
    @Composable
    private fun Preview() {
        Root()
    }

    private fun navigateToSignUp() = navigateTo(OnboardingFragmentDirections.actionOnboardingFragmentToSignUpFragment())
    private fun navigateToLogin() = navigateTo(OnboardingFragmentDirections.actionOnboardingFragmentToLoginFragment())
}