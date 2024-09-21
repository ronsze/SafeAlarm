package com.myproject.safealarm.feature.role.guard

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
class GuardRegisterViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
): BaseViewModel() {
    private val _uiState: MutableStateFlow<GuardRegisterUiState> = MutableStateFlow(GuardRegisterUiState.Loading)
    val uiState get() = _uiState.asStateFlow()

    private lateinit var mSocket: Socket
    private lateinit var wardUid: String
    private var uid: String = ""

    fun loadData() {
        getUserUseCase(
            scope = viewModelScope,
            onSuccess = {
                it?.run {
                    this@GuardRegisterViewModel.uid = uid
                    socketConnect()
                } ?: _uiState.set(GuardRegisterUiState.Failed)
            },
            onFailure = {

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
            on(SocketEvents.CONNECT_DONE, onConnectDone)
        }
    }

    private val onSocketConnected = Emitter.Listener {
        _uiState.set(GuardRegisterUiState.QRScanning)
    }

    fun connect(wardUid: String) {
        this.wardUid = wardUid
        mSocket.emit(SocketEvents.ENTER_ROOM, uid, wardUid)
    }

    private val onEnteredRoom = Emitter.Listener {
        mSocket.emit(SocketEvents.GUARD_ID, uid)
    }

    private val onConnectDone = Emitter.Listener {
        val profile = UserProfile(
            uid = uid,
            partnerId = wardUid,
            role = UserRole.GUARD
        )
        updateUserProfileUseCase(
            profile = profile,
            scope = viewModelScope,
            onSuccess = {
                mSocket.disconnect()
                _uiState.set(GuardRegisterUiState.Connected)
            },
            onFailure = {

            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        mSocket.close()
    }

    sealed interface GuardRegisterUiState {
        data object QRScanning: GuardRegisterUiState
        data object Loading: GuardRegisterUiState
        data object Failed: GuardRegisterUiState
        data object Connected: GuardRegisterUiState
    }
}