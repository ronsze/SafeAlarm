package com.myproject.safealarm.feature.guard.alarm

import android.widget.NumberPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.myproject.safealarm.R
import com.myproject.safealarm.ui.composable.BaseText
import com.myproject.safealarm.ui.composable.BasicButton
import kr.sdbk.domain.model.guard.Alarm

@Composable
fun AddAlarmDialog(
    onConfirm: (Alarm) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Content(
            onConfirm = onConfirm,
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
private fun Content(
    onConfirm: (Alarm) -> Unit,
    onDismissRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(15.dp)
    ) {
        val hours = (0..23).toList()
        var hour by remember { mutableStateOf(hours.first()) }

        val minuets = (0..59).toList()
        var minute by remember { mutableStateOf(minuets.first()) }

        Row {
            AndroidView(factory = {
                NumberPicker(it).apply {
                    minValue = 0
                    maxValue = 23
                    value = hour
                    setOnValueChangedListener { picker, oldVal, newVal ->
                        hour = newVal
                    }
                }
            }, update = {
                it.value = hour
            })
            BaseText(
                text = "시",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )


            AndroidView(factory = {
                NumberPicker(it).apply {
                    minValue = 0
                    maxValue = 59
                    value = 0
                    setOnValueChangedListener { picker, oldVal, newVal ->
                        minute = newVal
                        if (oldVal == minuets.last() && newVal == minuets.first()) hour++
                        else if (oldVal == minuets.first() && newVal == minuets.last()) hour--
                    }
                }
            }, update = {
                it.value = minute
            })
            BaseText(
                text = "분",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row {
            BasicButton(
                text = stringResource(id = R.string.confirm),
                onClick = { onConfirm(Alarm(hour, minute)) }
            )
            Spacer(modifier = Modifier.width(15.dp))

            BasicButton(
                text = stringResource(id = R.string.cancel),
                onClick = onDismissRequest
            )
        }
    }
}