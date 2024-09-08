package com.myproject.safealarm.feature.guard.info

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
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
import kr.sdbk.domain.model.Gender
import kr.sdbk.domain.model.ward.WardInfo

class WardInfoFragment: BaseFragment<WardInfoViewModel>() {
    override val fragmentViewModel: WardInfoViewModel by viewModels()

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

        val uiState = fragmentViewModel.uiState.collectAsStateWithLifecycle().value
        when (uiState) {
            WardInfoViewModel.WardInfoUiState.Loading -> LoadingView()
            is WardInfoViewModel.WardInfoUiState.Loaded -> View(info = uiState.data)
            WardInfoViewModel.WardInfoUiState.Failed -> Unit
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    private fun View(
        info: WardInfo?
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imageUri = remember { mutableStateOf(info?.imageUri ?: "") }
            val name = remember { mutableStateOf(info?.name ?: "") }
            val gender = remember { mutableStateOf(info?.gender ?: Gender.MALE) }
            val height = remember { mutableStateOf(info?.height ?: "") }
            val age = remember { mutableStateOf(info?.age ?: "") }
            val guardNumber = remember { mutableStateOf(info?.guardNumber ?: "") }
            val signalment = remember { mutableStateOf(info?.signalment ?: "") }

            val photoResultLauncher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        imageUri.value = uri.toString()
                    }
                }
            }

            BaseToolbar(
                frontComposable = BaseToolbarDefaults.defaultToolbarPainter(
                    onClick = this@WardInfoFragment::popupBackStack
                ),
                rearComposable = BaseToolbarDefaults.defaultToolbarPainter(
                    icon = Icons.Filled.Check,
                    onClick = {
                        val isInfoFilled = name.value.isNotEmpty() && height.value.isNotEmpty() && age.value.isNotEmpty() && guardNumber.value.isNotEmpty()
                        if (isInfoFilled) {
                            fragmentViewModel.saveInfo(
                                WardInfo(
                                    imageUri = imageUri.value,
                                    name = name.value,
                                    gender = gender.value,
                                    height = height.value,
                                    age = age.value,
                                    guardNumber = guardNumber.value,
                                    signalment = signalment.value
                                )
                            )
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.please_enter_every_info), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                GlideImage(
                    model = imageUri,
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clickable { takePhoto(photoResultLauncher) }
                )
            }

            InputBox(
                hint = stringResource(id = R.string.name),
                text = name,
                fontSize = 19.sp
            )
            Spacer(modifier = Modifier.height(5.dp))

            Row {
                GenderField(
                    gender = gender,
                    modifier = Modifier.weight(1f)
                )

                InputBox(
                    hint = stringResource(id = R.string.height),
                    text = height,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )

                InputBox(
                    hint = stringResource(id = R.string.age),
                    text = age,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            
            InputBox(
                hint = stringResource(id = R.string.signalment),
                text = signalment,
                fontSize = 15.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun GenderField(
        gender: MutableState<Gender>,
        modifier: Modifier = Modifier
    ) {
        var expended by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expended,
            onExpandedChange = { expended = !expended },
            modifier = modifier
        ) {
            BaseText(
                text = stringResource(id = getGenderText(gender.value))
            )
            ExposedDropdownMenu(
                expanded = expended,
                onDismissRequest = { expended = false }
            ) {
                Gender.entries.forEach {
                    DropdownMenuItem(
                        text = {
                            BaseText(
                                text = stringResource(id = getGenderText(it)),
                            )
                        },
                        onClick = {
                            gender.value = it
                            expended = false
                        }
                    )
                }
            }
        }
    }

    private fun getGenderText(gender: Gender) = when (gender) {
        Gender.MALE -> R.string.male
        Gender.FEMALE -> R.string.female
    }

    @Composable
    private fun InputBox(
        hint: String,
        text: MutableState<String>,
        fontSize: TextUnit,
        modifier: Modifier = Modifier
    ) {
        BasicTextField(
            value = text.value,
            onValueChange = { text.value = it },
            textStyle = TextStyle.Default.copy(
                color = Color.Black,
                fontSize = fontSize
            ),
            modifier = modifier
        ) {
            if (text.value.isEmpty()) {
                BaseText(
                    text = hint,
                    color = Color.LightGray,
                    fontSize = fontSize
                )
            } else {
                it()
            }
        }
    }

    private fun takePhoto(launcher: ActivityResultLauncher<Intent>) {
        launcher.launch(
            Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf("image/jpeg", "image/png", "image/bmp", "image/webp")
                )
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            }
        )
    }
}