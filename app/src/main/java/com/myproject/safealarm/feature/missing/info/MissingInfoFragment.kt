package com.myproject.safealarm.feature.missing.info

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.myproject.safealarm.R
import com.myproject.safealarm.base.BaseFragment
import com.myproject.safealarm.ui.composable.BaseText
import com.myproject.safealarm.ui.composable.BaseToolbar
import com.myproject.safealarm.ui.composable.BaseToolbarDefaults
import com.myproject.safealarm.util.getGenderText
import dagger.hilt.android.AndroidEntryPoint
import kr.sdbk.domain.model.ward.MissingInfo

@AndroidEntryPoint
class MissingInfoFragment: BaseFragment<MissingInfoViewModel>() {
    override val fragmentViewModel: MissingInfoViewModel by viewModels()

    @Composable
    override fun Root() {
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(key1 = lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_CREATE) fragmentViewModel.loadData()
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }
    
    @Composable
    private fun View() {
        Column {
            BaseToolbar(
                frontComposable = BaseToolbarDefaults.defaultToolbarPainter(
                    onClick = this@MissingInfoFragment::popupBackStack
                ),
                titleComposable = BaseToolbarDefaults.defaultTitle(
                    title = stringResource(id = R.string.missing_info)
                )
            )

            val missingList by fragmentViewModel.missingList.collectAsStateWithLifecycle()
            LazyColumn {
                items(missingList) {
                    MissingItem(it)
                }
            }
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    private fun MissingItem(
        info: MissingInfo
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navigateToEachInfo(info) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlideImage(
                    model = info.wardInfo.imageUri,
                    contentDescription = "",
                    modifier = Modifier.size(125.dp)
                )
                Column {
                    BaseText(
                        text = info.wardInfo.name,
                        fontSize = 16.sp
                    )
                    Row {
                        BaseText(
                            text = stringResource(id = getGenderText(info.wardInfo.gender)),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        BaseText(
                            text = info.wardInfo.age,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    BaseText(
                        text = info.missingTime,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            BaseText(
                text = info.lastLocation.text,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    private fun navigateToEachInfo(info: MissingInfo) = navigateTo(MissingInfoFragmentDirections.actionMissingInfoFragmentToEachInfoFragment(info))
}