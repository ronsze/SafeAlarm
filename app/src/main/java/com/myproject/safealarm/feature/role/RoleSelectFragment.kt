package com.myproject.safealarm.feature.role

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import com.myproject.safealarm.R
import com.myproject.safealarm.base.BaseFragment
import com.myproject.safealarm.ui.composable.BaseText
import com.myproject.safealarm.ui.composable.BaseToolbar
import com.myproject.safealarm.ui.composable.BaseToolbarDefaults
import com.myproject.safealarm.ui.composable.BasicButton
import com.myproject.safealarm.ui.composable.noRippleClickable
import com.myproject.safealarm.ui.theme.SkyBlue
import kr.sdbk.domain.model.user.UserRole

class RoleSelectFragment: BaseFragment<RoleSelectViewModel>() {
    override val fragmentViewModel: RoleSelectViewModel by viewModels()

    @Composable
    override fun Root() {
        Column {
            BaseToolbar(
                frontComposable = BaseToolbarDefaults.defaultToolbarPainter(
                    onClick = this@RoleSelectFragment::popupBackStack
                )
            )
            Spacer(modifier = Modifier.height(40.dp))

            var selectedRole by remember { mutableStateOf(UserRole.GUARD) }
            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                RoleCard(
                    image = R.drawable.guard_img,
                    text = R.string.guard,
                    isSelected = selectedRole == UserRole.GUARD,
                    onClick = { selectedRole = UserRole.GUARD }
                )
                Spacer(modifier = Modifier.height(35.dp))

                RoleCard(
                    image = R.drawable.ward_img,
                    text = R.string.ward,
                    isSelected = selectedRole == UserRole.WARD,
                    onClick = { selectedRole = UserRole.WARD }
                )
                Spacer(modifier = Modifier.weight(1f))

                BasicButton(
                    text = stringResource(id = R.string.confirm)
                ) {
                    when (selectedRole) {
                        UserRole.GUARD -> navigateToGuardRegister()
                        UserRole.WARD -> navigateToWardRegister()
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    @Composable
    private fun RoleCard(
        @DrawableRes image: Int,
        @StringRes text: Int,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        val borderColor = if (isSelected) SkyBlue else Color.LightGray
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .border(2.dp, borderColor, RoundedCornerShape(55.dp))
                .padding(15.dp)
                .noRippleClickable { onClick() }
        ) {
            Image(
                painter = painterResource(id = image),
                contentDescription = "",
                modifier = Modifier
                    .size(85.dp)
            )

            BaseText(
                text = stringResource(id = text),
                fontSize = 30.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
            )
        }
    }

    @Preview
    @Composable
    private fun RoleCardPreview() {
        RoleCard(image = R.drawable.guard_img, text = R.string.ward, isSelected = true) {

        }
    }

    private fun navigateToGuardRegister() = navigateTo(RoleSelectFragmentDirections.actionRoleSelectFragmentToGuardRegisterFragment())
    private fun navigateToWardRegister() = navigateTo(RoleSelectFragmentDirections.actionRoleSelectFragmentToWardRegisterFragment())
}