package com.myproject.safealarm.feature.guard

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.myproject.safealarm.base.BaseViewModel
import com.myproject.safealarm.util.SocketEvents
import com.myproject.safealarm.util.Values
import dagger.hilt.android.lifecycle.HiltViewModel
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.sdbk.domain.usecase.user_service.GetUserProfileUseCase
import java.net.URISyntaxException
import javax.inject.Inject

@HiltViewModel
class GuardViewModel @Inject constructor(
    private val getProfileUseCase: GetUserProfileUseCase
): BaseViewModel() {
    private val _uiState: MutableStateFlow<GuardUiState> = MutableStateFlow(GuardUiState.Loading)
    val uiState get() = _uiState.asStateFlow()

    private lateinit var mSocket: Socket

    fun connect() {
        try {
            mSocket = IO.socket(Values.BASE_URL)
            if (mSocket.connected()) {
                _uiState.set(GuardUiState.Connected)
            } else {
                with (mSocket) {
                    connect()
                    on(Socket.EVENT_CONNECT, onConnected)
                    on(SocketEvents.ENTERED_ROOM, onEnteredRoom)
                }
            }
        } catch (e: URISyntaxException) {
            Log.e("qweqwe", e.message.toString())
        }
    }

    private val onConnected = Emitter.Listener {
        getProfileUseCase(
            scope = viewModelScope,
            onSuccess = { profile ->
                mSocket.emit(SocketEvents.ENTER_ROOM, profile.partnerId)
            },
            onFailure = {

            }
        )
    }

    private val onEnteredRoom = Emitter.Listener {
        _uiState.set(GuardUiState.Connected)
    }

    sealed interface GuardUiState {
        data object Loading: GuardUiState
        data object Connected: GuardUiState
    }
}