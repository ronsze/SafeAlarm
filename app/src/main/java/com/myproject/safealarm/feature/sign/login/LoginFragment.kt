package com.myproject.safealarm.feature.sign.login

import android.util.Patterns
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myproject.safealarm.R
import com.myproject.safealarm.base.BaseFragment
import com.myproject.safealarm.ui.composable.BaseText
import com.myproject.safealarm.ui.composable.BaseToolbar
import com.myproject.safealarm.ui.composable.BaseToolbarDefaults
import com.myproject.safealarm.ui.composable.BasicButton
import com.myproject.safealarm.ui.composable.PlaceHolderTextField

class LoginFragment: BaseFragment<LoginViewModel>() {
    override val fragmentViewModel: LoginViewModel by viewModels()

    @Composable
    override fun Root() {
        val uiState by fragmentViewModel.uiState.collectAsStateWithLifecycle()
        when (uiState) {
            LoginViewModel.LoginUiState.View -> View()
            LoginViewModel.LoginUiState.LoggedIn -> navigateToSplash()
        }
    }

    @Composable
    private fun View() {
        Column {
            BaseToolbar(
                frontComposable = BaseToolbarDefaults.defaultToolbarPainter(
                    onClick = this@LoginFragment::popupBackStack
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            BaseText(
                text = stringResource(id = R.string.login),
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            val email = remember { mutableStateOf("") }
            val password = remember { mutableStateOf("") }
            val confirmButtonEnabled by remember(email, password) {
                derivedStateOf {
                    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email.value).matches()
                    val isPasswordValid = password.value.length >= 8
                    isEmailValid && isPasswordValid
                }
            }

            InputLayer(
                email = email,
                password = password
            )
            Spacer(modifier = Modifier.weight(1f))

            BasicButton(
                text = stringResource(id = R.string.confirm),
                enabled = confirmButtonEnabled
            ) {
                fragmentViewModel.login(email.value, password.value)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    @Composable
    private fun InputLayer(
        email: MutableState<String>,
        password: MutableState<String>
    ) {
        val passwordError by fragmentViewModel.passwordError.collectAsStateWithLifecycle()

        PlaceHolderTextField(
            text = email.value,
            placeholder = stringResource(id = R.string.email),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            email.value = it
        }
        Spacer(modifier = Modifier.height(16.dp))

        PlaceHolderTextField(
            text = password.value,
            placeholder = stringResource(id = R.string.password),
            error = when (passwordError) {
                LoginViewModel.PasswordError.None -> ""
                LoginViewModel.PasswordError.WrongEmailOrPassword -> stringResource(id = R.string.wrong_email_or_password)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = { TransformedText(AnnotatedString("●".repeat(password.value.length)), OffsetMapping.Identity)},
            modifier = Modifier
                .fillMaxWidth()
        ) {
            password.value = it
            fragmentViewModel.resetPasswordError()
        }
    }

    private fun navigateToSplash() = navigateTo(LoginFragmentDirections.actionLoginFragmentToSplashNav())
}