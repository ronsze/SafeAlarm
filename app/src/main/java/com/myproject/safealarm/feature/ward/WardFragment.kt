package com.myproject.safealarm.feature.ward

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.myproject.safealarm.R
import com.myproject.safealarm.base.BaseFragment
import com.myproject.safealarm.ui.composable.MenuCard

class WardFragment: BaseFragment<WardViewModel>() {
    override val fragmentViewModel: WardViewModel by viewModels()

    @Composable
    override fun Root() {
        LazyVerticalGrid(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .padding(horizontal = 20.dp)
        ) {
            item {
                MenuCard(
                    image = R.drawable.help_img,
                    label = R.string.help,
                    onClick = this@WardFragment::help
                )
            }

            item {
                MenuCard(
                    image = R.drawable.missing_img,
                    label = R.string.missing_register,
                    onClick = this@WardFragment::navigateToMissingInfo
                )
            }

            item {
                MenuCard(
                    image = R.drawable.set_img,
                    label = R.string.setting,
                    onClick = this@WardFragment::navigateToSetting
                )
            }
        }
    }

    private fun help() {
//        val intent = Intent(this, ForegroundService::class.java)
//        intent.action = Actions.HELP_CALL_WARD
//        startService(intent)
    }
    private fun navigateToMissingInfo() = navigateTo(WardFragmentDirections.actionWardFragmentToMissingNav())
    private fun navigateToSetting() = navigateTo(WardFragmentDirections.actionWardFragmentToWardSettingFragment())
}