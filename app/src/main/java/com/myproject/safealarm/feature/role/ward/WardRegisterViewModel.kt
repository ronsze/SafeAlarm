package com.myproject.safealarm.feature.role.ward

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
import kr.sdbk.domain.model.user.UserProfile
import kr.sdbk.domain.model.user.UserRole
import kr.sdbk.domain.usecase.user_auth.GetUserUseCase
import kr.sdbk.domain.usecase.user_service.UpdateUserProfileUseCase
import java.net.URISyntaxException
import javax.inject.Inject


@HiltViewModel
class WardRegisterViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
): BaseViewModel() {
    private val _uiState: MutableStateFlow<WardRegisterUiState> = MutableStateFlow(WardRegisterUiState.Loading)
    val uiState get() = _uiState.asStateFlow()

    private lateinit var mSocket: Socket
    private lateinit var guardUid: String
    private var uid: String = ""

    fun loadData() {
        getUserUseCase(
            scope = viewModelScope,
            onSuccess = { user ->
                user?.run {
                    this@WardRegisterViewModel.uid = uid
                    socketConnect()
                } ?: _uiState.set(WardRegisterUiState.Failed)
            },
            onFailure = {
                _uiState.set(WardRegisterUiState.Failed)
                basicErrorHandling(it)
            }
        )
    }

    private fun socketConnect() {
        try {
            mSocket = IO.socket(Values.BASE_URL)
            mSocket.connect()
        } catch (e: URISyntaxException) {
            Log.e("ee", e.message.toString())
        }

        with(mSocket) {
            on(Socket.EVENT_CONNECT, onSocketConnected)
            on(SocketEvents.ENTERED_ROOM, onEnteredRoom)
            on(SocketEvents.GUARD_ID, onGuardIdReceived)
            on(SocketEvents.CONNECT_DONE, onConnectDone)
        }
    }

    private val onSocketConnected = Emitter.Listener {
        mSocket.emit(SocketEvents.ENTER_ROOM, uid, uid)
    }

    private val onEnteredRoom = Emitter.Listener {
        _uiState.set(WardRegisterUiState.Loaded(uid))
    }

    private val onGuardIdReceived = Emitter.Listener {
        guardUid = it[0].toString()
        mSocket.emit(SocketEvents.CONNECT_DONE)
    }

    private val onConnectDone = Emitter.Listener {
        val profile = UserProfile(
            uid = uid,
            partnerId = guardUid,
            role = UserRole.WARD
        )
        updateUserProfileUseCase(
            profile = profile,
            scope = viewModelScope,
            onSuccess = {
                mSocket.disconnect()
                _uiState.set(WardRegisterUiState.Connected)
            },
            onFailure = {

            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        mSocket.close()
    }

    sealed interface WardRegisterUiState {
        data object Loading: WardRegisterUiState
        data class Loaded(val uid: String): WardRegisterUiState
        data object Failed: WardRegisterUiState
        data object Connected: WardRegisterUiState
    }
}