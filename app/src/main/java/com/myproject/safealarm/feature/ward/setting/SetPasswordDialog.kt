package com.myproject.safealarm.feature.ward.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myproject.safealarm.R
import com.myproject.safealarm.ui.composable.BaseText
import com.myproject.safealarm.ui.composable.HintTextField

@Composable
fun SetPasswordDialog(
    onConfirm: (String, String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val previousPassword = remember { mutableStateOf("") }
    val newPassword = remember { mutableStateOf("") }

    AlertDialog(
        title = {
            BaseText(
                text = stringResource(id = R.string.change_verification_password)
            )
        },
        text = {
            Column {
                HintTextField(
                    hint = stringResource(id = R.string.previous_password),
                    text = previousPassword,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                HintTextField(
                    hint = stringResource(id = R.string.new_password),
                    text = newPassword,
                    fontSize = 14.sp
                )
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            BaseText(
                text = stringResource(id = R.string.confirm),
                modifier = Modifier.clickable { onConfirm(previousPassword.value, newPassword.value) }
            )
        },
        dismissButton = {
            BaseText(
                text = stringResource(id = R.string.cancel),
                modifier = Modifier.clickable { onDismissRequest() }
            )
        }
    )
}

@Composable
private fun Content() {

}