package com.myproject.safealarm.ui.composable

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuCard(
    @DrawableRes image: Int,
    @StringRes label: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(15.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp)
    ) {
        Spacer(modifier = Modifier.height(15.dp))

        Image(
            painter = painterResource(id = image),
            contentDescription = "",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        BaseText(
            text = stringResource(id = label),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}