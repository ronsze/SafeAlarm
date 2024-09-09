package com.myproject.safealarm.ui.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myproject.safealarm.ui.theme.SkyBlue

@Composable
fun BasicButton(
    text: String,
    color: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = SkyBlue
    ),
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val textColor = if (enabled) Color.White else Color.Black
    Button(
        colors = color,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp)
    ) {
        BaseText(
            text = text,
            fontSize = 16.sp,
            color = textColor
        )
    }
}