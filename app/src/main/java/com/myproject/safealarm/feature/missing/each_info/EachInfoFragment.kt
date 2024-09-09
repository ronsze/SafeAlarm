package com.myproject.safealarm.feature.missing.each_info

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.myproject.safealarm.R
import com.myproject.safealarm.base.BaseFragment
import com.myproject.safealarm.ui.composable.BaseText
import com.myproject.safealarm.ui.composable.BaseToolbar
import com.myproject.safealarm.ui.composable.BaseToolbarDefaults
import com.myproject.safealarm.util.getGenderText
import com.naver.maps.geometry.LatLng

class EachInfoFragment: BaseFragment<EachInfoViewModel>() {
    override val fragmentViewModel: EachInfoViewModel by viewModels()
    private val args: EachInfoFragmentArgs by navArgs()

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    override fun Root() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BaseToolbar(
                frontComposable = BaseToolbarDefaults.defaultToolbarPainter(
                    onClick = this@EachInfoFragment::popupBackStack
                ),
                titleComposable = BaseToolbarDefaults.defaultTitle(
                    title = stringResource(id = R.string.missing_detail)
                )
            )

            GlideImage(
                model = args.missingInfo.wardInfo.imageUri,
                contentDescription = "",
                modifier = Modifier
                    .size(350.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            InfoLayer()
        }
    }

    @Composable
    private fun InfoLayer() {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(horizontal = 10.dp)
        ) {
            BaseText(
                text = args.missingInfo.wardInfo.name,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(5.dp))

            Row {
                InfoText(text = stringResource(id = getGenderText(args.missingInfo.wardInfo.gender)))
                InfoText(text = "${stringResource(id = R.string.age)} ${args.missingInfo.wardInfo.age}")
                InfoText(text = "${stringResource(id = R.string.height)} ${args.missingInfo.wardInfo.height}cm")
            }
            Spacer(modifier = Modifier.height(5.dp))

            BaseText(
                text = "${stringResource(id = R.string.guard_number)} ${args.missingInfo.wardInfo.guardNumber}",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(5.dp))

            BaseText(
                text = "${stringResource(id = R.string.missing_time)} ${args.missingInfo.missingTime}",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(5.dp))

            BaseText(
                text = "${stringResource(id = R.string.guard_number)} ${args.missingInfo.lastLocation.text}",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val lastLocation = LatLng(args.missingInfo.lastLocation.latitude, args.missingInfo.lastLocation.longitude)
                        navigateToMissingMap(lastLocation)
                    }
            )
            Spacer(modifier = Modifier.height(5.dp))

            BaseText(
                text = "${stringResource(id = R.string.signalment)} ${args.missingInfo.signalment}",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(5.dp))

            BaseText(
                text = "${stringResource(id = R.string.extra)} ${args.missingInfo.extra}",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    @Composable
    private fun RowScope.InfoText(
        text: String,
    ) {
        BaseText(
            text = text,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }

    private fun navigateToMissingMap(lastLocation: LatLng) = navigateTo(EachInfoFragmentDirections.actionEachInfoFragmentToMissingMapFragment(lastLocation))
}