package com.myproject.safealarm.feature.role.ward

import androidx.lifecycle.viewModelScope
import com.myproject.safealarm.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.sdbk.domain.usecase.user_auth.GetUserUseCase
import javax.inject.Inject

@HiltViewModel
class WardRegisterViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase
): BaseViewModel() {
    private val _uiState: MutableStateFlow<WardRegisterUiState> = MutableStateFlow(WardRegisterUiState.Loading)
    val uiState get() = _uiState.asStateFlow()

    fun loadData() {
        getUserUseCase(
            scope = viewModelScope,
            onSuccess = { user ->
                user?.run { _uiState.set(WardRegisterUiState.Loaded(uid)) } ?: _uiState.set(WardRegisterUiState.Failed)
            },
            onFailure = {
                _uiState.set(WardRegisterUiState.Failed)
                basicErrorHandling(it)
            }
        )
    }

//    private val onReceivePrime = Emitter.Listener{
//        var msg = it[0].toString().split("9y6s0y9")
//        var remoteID = msg[0]
//        val primeMsg = msg[1]
//        Singleton.server.getCert(remoteID).enqueue(object:Callback<ResponseDC>{
//            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
//                val certificate = response.body()!!.result!!
//
//                Singleton.server.getCRL().enqueue(object:Callback<ResponseDC>{
//                    override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
//                        val path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
//                        val crl = loadCRL(response.body()!!.result!!, path)
//                        saveCertificate(certificate, path, crl)
//                        sendR1(primeMsg)
//                    }
//                    override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
//                        Log.e("인증서", "실패")
//                    }
//                })
//            }
//            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
//                Log.e("인증서", "실패")
//            }
//        })
//    }
//
//    private fun sendR1(primeMsg: String){
//        val primeNum = primeMsg.split("SiGn")[0]
//        if(checkSign(primeMsg, getPublicKey())){
//            val primeArr = primeNum.split(".")
//            this.p = primeArr[0].toBigInteger()
//            this.g = primeArr[1].toBigInteger()
//            this.x = getX()
//            val r1 = g.modPow(x, p)
//            mSocketR.emit("sendR1", r1.toString()+ getSign(r1.toString()))
//        }else{
//            Log.e("서명 sendR1", "서명 불일치")
//        }
//    }
//
//    private fun getX(): BigInteger {
//        var x = BigInteger(1024, Random())
//        while(x > p.subtract(1.toBigInteger())){
//            x = BigInteger(1024, Random())
//        }
//        return x
//    }
//
//    private val onReceiveR2 = Emitter.Listener {
//        val msg = it[0].toString()
//        val r2 = msg.split("SiGn")[0].toBigInteger()
//        if(checkSign(msg, getPublicKey())){
//            saveShardKey(r2, x)
//            registGuard()
//        }else{
//            Log.e("서명 receiveR2", "서명 불일치")
//        }
//    }
//
//    private fun saveShardKey(r2: BigInteger, x: BigInteger){
//        val shardKey = r2.modPow(x, p)
//        App.prefs.shardKey = shardKey.toString()
//    }
//
//    fun registGuard(){                          //보호자 등록
//        Singleton.server.registGuard(App.prefs.id).enqueue(object: Callback<ResponseDC> {
//            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
//                Toast.makeText(context, "등록되었습니다.", Toast.LENGTH_SHORT).show()
//                App.prefs.regKey = true
//                App.prefs.role = "Guard"
//                App.prefs.room = App.prefs.id
//                startForeService()
//            }
//
//            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
//                Toast.makeText(context, "다시 시도해주세요.", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }

    sealed interface WardRegisterUiState {
        data object Loading: WardRegisterUiState
        data class Loaded(val uid: String): WardRegisterUiState
        data object Failed: WardRegisterUiState
    }
}