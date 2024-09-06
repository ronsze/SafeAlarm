package com.myproject.safealarm.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel: ViewModel() {
    protected fun basicErrorHandling(error: Throwable) {
        Log.e("Error", error.message.toString())
    }

    protected fun<T> MutableStateFlow<T>.set(value: T) = viewModelScope.launch { emit(value) }
}