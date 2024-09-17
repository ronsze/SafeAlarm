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
import java.math.BigInteger
import java.net.URISyntaxException
import java.security.SecureRandom
import javax.inject.Inject

@HiltViewModel
class GuardRegisterViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
): BaseViewModel() {
    private val _uiState: MutableStateFlow<GuardRegisterUiState> = MutableStateFlow(GuardRegisterUiState.QRScanning)
    val uiState get() = _uiState.asStateFlow()

    private lateinit var mSocket: Socket
    private lateinit var wardUid: String
    private var uid: String = ""

    private val p: BigInteger = BigInteger(Values.pString, 16)
    private val g: BigInteger = BigInteger(Values.gString, 16)
    private lateinit var x: BigInteger
    private lateinit var k: BigInteger

    fun connect(wardUid: String) {
        _uiState.set(GuardRegisterUiState.Loading)

        this.wardUid = wardUid
        getUserUseCase(
            scope = viewModelScope,
            onSuccess = { user ->
                user?.uid?.run {
                    this@GuardRegisterViewModel.uid = this
                    connect()
                }
            },
            onFailure = {

            }
        )
    }

    private fun connect() {
        try {
            mSocket = IO.socket(Values.BASE_URL)
            mSocket.connect()
        } catch (e: URISyntaxException) {
            Log.e("ee", e.message.toString())
        }

        with(mSocket) {
            on(Socket.EVENT_CONNECT, onSocketConnected)
            on(SocketEvents.ENTERED_ROOM, onEnteredRoom)
            on(SocketEvents.SEND_R2, onReceiveR2)
        }
    }

    private val onSocketConnected = Emitter.Listener {
        mSocket.emit(SocketEvents.ENTER_ROOM, wardUid)
    }

    private val onEnteredRoom = Emitter.Listener {
        sendR1(p, g)
    }

    private fun sendR1(p: BigInteger, g: BigInteger) {
        val x = BigInteger(1024, SecureRandom()).mod(p)
        val r1 = g.modPow(x, p)

        this.x = x
        mSocket.send(SocketEvents.SEND_R1, r1)
    }

    private val onReceiveR2 = Emitter.Listener {
        val r2 = it[0].toString().toBigInteger()
        k = r2.modPow(x, p)

        mSocket.emit(SocketEvents.KEY_EXCHANGED)
        updateProfile()
    }

    private fun updateProfile() {
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