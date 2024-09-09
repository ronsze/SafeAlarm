package com.myproject.safealarm.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.myproject.safealarm.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap

abstract class BaseFragment<V>: Fragment() {
    abstract val fragmentViewModel: V

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                Root()
            }
        }
    }

    @Composable
    abstract fun Root()

    @Composable
    protected fun LoadingView() {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.loading_img),
                contentDescription = "",
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.Center)
            )
        }
    }

    protected fun navigateTo(direction: NavDirections) = findNavController().navigate(direction)
    protected fun popupBackStack() {
        if (findNavController().currentBackStackEntry == null) requireActivity().finish()
        else findNavController().popBackStack()
    }

    protected fun NaverMap.moveCamera(latLng: LatLng, zoom: Double = 13.0) {
        val cameraUpdate = CameraUpdate.scrollAndZoomTo(latLng, zoom)
        moveCamera(cameraUpdate)
    }

    protected fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}