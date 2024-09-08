package com.myproject.safealarm.feature.guard.setting

import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.myproject.safealarm.R
import com.myproject.safealarm.ui.composable.BaseText

@Composable
fun DeleteWardInfoDialog(
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            BaseText(
                text = stringResource(id = R.string.confirm),
                modifier = Modifier
                    .clickable {
                        onDismissRequest()
                        onConfirm()
                    }
            )
        },
        dismissButton = {
            BaseText(
                text = stringResource(id = R.string.cancel),
                modifier = Modifier
                    .clickable { onDismissRequest() }
            )
        }
    )
}