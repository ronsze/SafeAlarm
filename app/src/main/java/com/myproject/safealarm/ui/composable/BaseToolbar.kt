package com.myproject.safealarm.ui.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BaseToolbar(
    modifier: Modifier = Modifier,
    titleComposable: @Composable () -> Unit = {},
    frontComposable: @Composable () -> Unit = {},
    rearComposable: @Composable () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 20.dp)
    ) {
        frontComposable()
        titleComposable()
        rearComposable()
    }
}


object BaseToolbarDefaults {
    @Composable
    fun defaultTitle(
        title: String = "",
        titleSize: TextUnit = 16.sp,
        titleColor: Color = Color.Black,
        titleWeight: FontWeight = FontWeight.Medium
    ): @Composable () -> Unit = {
        BaseText(
            text = title,
            fontSize = titleSize,
            color = titleColor,
            fontWeight = titleWeight,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    fun defaultToolbarPainter(
        @DrawableRes drawable: Int,
        size: Size = Size(24f, 24f),
        modifier: Modifier = Modifier,
        onClick: () -> Unit = {}
    ): @Composable () -> Unit = {
        Image(
            painter = painterResource(id = drawable),
            contentDescription = "",
            modifier = modifier
                .width(size.width.dp)
                .height(size.height.dp)
                .clickable { onClick() }
        )
    }

    @Composable
    fun defaultToolbarPainter(
        icon: ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
        size: Size = Size(24f, 24f),
        modifier: Modifier = Modifier,
        onClick: () -> Unit = {}
    ): @Composable () -> Unit = {
        Image(
            imageVector = icon,
            contentDescription = "",
            modifier = modifier
                .width(size.width.dp)
                .height(size.height.dp)
                .clickable { onClick() }
        )
    }
}