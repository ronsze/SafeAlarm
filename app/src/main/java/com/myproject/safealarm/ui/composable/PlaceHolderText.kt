package com.myproject.safealarm.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myproject.safealarm.ui.theme.SkyBlue

@Composable
fun PlaceHolderText(
    text: String,
    fontSize: TextUnit = 14.sp,
    placeholder: String = "",
    colors: PlaceHolderTextDefaults.PlaceHolderTextColors = PlaceHolderTextDefaults.colors(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        if (placeholder.isNotEmpty()) {
            BaseText(
                text = placeholder,
                fontSize = 12.sp,
                color = colors.placeholderColor
            )
            Spacer(modifier = Modifier.height(3.dp))
        }
        BaseText(
            text = text,
            fontSize = fontSize,
            color = colors.textColor
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(colors.dividerColor)
        )
    }
}

object PlaceHolderTextDefaults {
    @Composable
    fun colors(
        textColor: Color = Color.Black,
        placeholderColor: Color = Color.LightGray,
        dividerColor: Color = Color.LightGray,
    ) = PlaceHolderTextColors(
        textColor = textColor,
        placeholderColor = placeholderColor,
        dividerColor = dividerColor
    )

    data class PlaceHolderTextColors(
        val textColor: Color,
        val placeholderColor: Color,
        val dividerColor: Color
    )
}

@Composable
fun PlaceHolderTextField(
    text: String,
    fontSize: TextUnit = 14.sp,
    hint: String = "",
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    colors: PlaceHolderTextFieldDefaults.PlaceHolderColors = PlaceHolderTextFieldDefaults.colors(),
    error: String = "",
    modifier: Modifier = Modifier,
    onValueChanged: (String) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        var isFocused by remember { mutableStateOf(false) }
        val onError = error.isNotEmpty()

        if (placeholder.isNotEmpty()) {
            val placeholderColor = if (onError) colors.errorPlaceholderColor else if (!isFocused) colors.disablePlaceholderColor else colors.placeholderColor
            BaseText(
                text = placeholder,
                fontSize = 14.sp,
                color = placeholderColor
            )
            Spacer(modifier = Modifier.height(3.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
        ) {
            BasicTextField(
                value = text,
                onValueChange = onValueChanged,
                textStyle = defaultTextStyle(
                    color = colors.textColor,
                    fontSize = fontSize
                ),
                keyboardOptions = keyboardOptions,
                visualTransformation = visualTransformation,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { isFocused = it.isFocused }
            ) {
                Box {
                    it()
                    if (text.isEmpty()) {
                        BaseText(
                            text = hint,
                            fontSize = fontSize,
                            color = colors.hintColor
                        )
                    }
                }
            }

            if (isFocused || text.isNotEmpty()) {
                Spacer(modifier = Modifier.width(3.dp))
                Image(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "",
                    modifier = Modifier
                        .size(16.dp)
                        .noRippleClickable { onValueChanged("") }
                )
            }
        }

        val dividerColor = if (onError) colors.errorDividerColor else if (!isFocused) colors.disableDividerColor else colors.dividerColor
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(dividerColor)
        )

        if (onError && error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            BaseText(
                text = error,
                fontSize = 12.sp,
                color = colors.errorTextColor,
            )
        }
    }
}

object PlaceHolderTextFieldDefaults {
    @Composable
    fun colors(
        textColor: Color = Color.Black,
        hintColor: Color = Color.Gray,
        placeholderColor: Color = SkyBlue,
        errorPlaceholderColor: Color = Color.Red,
        disablePlaceholderColor: Color = Color.Gray,
        disableDividerColor: Color = Color.LightGray,
        dividerColor: Color = SkyBlue,
        errorDividerColor: Color = Color.Red,
        errorTextColor: Color = Color.Red
    ) = PlaceHolderColors(
        textColor = textColor,
        hintColor = hintColor,
        placeholderColor = placeholderColor,
        errorPlaceholderColor = errorPlaceholderColor,
        disablePlaceholderColor = disablePlaceholderColor,
        disableDividerColor = disableDividerColor,
        dividerColor = dividerColor,
        errorDividerColor = errorDividerColor,
        errorTextColor = errorTextColor
    )

    data class PlaceHolderColors(
        val textColor: Color,
        val hintColor: Color,
        val placeholderColor: Color,
        val errorPlaceholderColor: Color,
        val disablePlaceholderColor: Color,
        val disableDividerColor: Color,
        val dividerColor: Color,
        val errorDividerColor: Color,
        val errorTextColor: Color
    )
}