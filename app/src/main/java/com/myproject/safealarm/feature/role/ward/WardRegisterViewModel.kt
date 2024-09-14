package com.myproject.safealarm.feature.role.ward

import androidx.lifecycle.viewModelScope
import com.myproject.safealarm.base.BaseViewModel
import com.myproject.safealarm.util.Secret
import com.myproject.safealarm.util.SocketEvents
import com.myproject.safealarm.util.SocketEvents.ENTER_ROOM
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
import java.math.BigInteger
import java.security.SecureRandom
import javax.inject.Inject

@HiltViewModel
class WardRegisterViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
): BaseViewModel() {
    private val _uiState: MutableStateFlow<WardRegisterUiState> = MutableStateFlow(WardRegisterUiState.Loading)
    val uiState get() = _uiState.asStateFlow()

    private lateinit var mSocket: Socket
    private var uid: String = ""

    private val p: BigInteger = BigInteger(Values.pString, 16)
    private val g: BigInteger = BigInteger(Values.gString, 16)
    private lateinit var k: BigInteger

    fun connect() {
        mSocket = IO.socket(Secret.SOCKET_URL).apply {
            connect()
            on(Socket.EVENT_CONNECT, onSocketConnected)
            on(SocketEvents.ENTERED_ROOM, onEnteredRoom)
            on(SocketEvents.SEND_R1, onReceiveR1)
            on(SocketEvents.KEY_EXCHANGED, onKeyExchanged)
        }
    }

    private val onSocketConnected = Emitter.Listener {
        loadData()
    }

    private fun loadData() {
        getUserUseCase(
            scope = viewModelScope,
            onSuccess = { user ->
                user?.run {
                    this@WardRegisterViewModel.uid = uid
                    mSocket.emit(ENTER_ROOM, uid)
                } ?: _uiState.set(WardRegisterUiState.Failed)
            },
            onFailure = {
                _uiState.set(WardRegisterUiState.Failed)
                basicErrorHandling(it)
            }
        )
    }

    private val onEnteredRoom = Emitter.Listener {
        _uiState.set(WardRegisterUiState.Loaded(uid))
    }

    private val onReceiveR1 = Emitter.Listener {
        val r1 = it[0].toString().toBigInteger()
        val y = BigInteger(1024, SecureRandom()).mod(p)
        val r2 = g.modPow(y, p)
        k = r1.modPow(y, p)

        mSocket.send(SocketEvents.SEND_R2, r2)
    }

    private val onKeyExchanged = Emitter.Listener {
        val guardUid = it[0].toString()
        updateProfile(guardUid)
    }

    private fun updateProfile(guardUid: String) {
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

    sealed interface WardRegisterUiState {
        data object Loading: WardRegisterUiState
        data class Loaded(val uid: String): WardRegisterUiState
        data object Failed: WardRegisterUiState
        data object Connected: WardRegisterUiState
    }
}