package com.myproject.safealarm.feature.role.guard

import com.myproject.safealarm.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class GuardRegisterViewModel: BaseViewModel() {
    private val _uiState: MutableStateFlow<GuardRegisterUiState> = MutableStateFlow(GuardRegisterUiState.QRScanning)
    val uiState get() = _uiState.asStateFlow()

    fun connect() {
        _uiState.set(GuardRegisterUiState.Loading)
    }

//    private fun startDHExchange(remoteID: String){
//        loadingDlog.show()
//        try{
//            mSocketR.emit("enterRoom", remoteID)
//            mSocketR.emit("getPrime", App.prefs.id)    //onCallbackPrime
//        }catch (e:Exception){
//            e.printStackTrace()
//        }
//    }
//
//    private val onCallbackPrime = Emitter.Listener {
//        Singleton.server.getCert(this.remoteID).enqueue(object:Callback<ResponseDC>{
//            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
//                val certificate = response.body()!!.result!!
//
//                Singleton.server.getCRL().enqueue(object: Callback<ResponseDC>{
//                    override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
//                        val path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
//                        val crl = loadCRL(response.body()!!.result!!, path)
//                        saveCertificate(certificate, path, crl) //result = X509certificate
//                        val primeNumber = it[0].toString()
//                        sendPrime(primeNumber)
//                    }
//
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
//    private fun sendPrime(primeNumber: String){
//        val primeNum = primeNumber.substring(2, primeNumber.length - 2)
//        val arr = primeNum.split(".")
//        this.p = arr[0].toBigInteger()
//        this.g = arr[1].toBigInteger()
//        mSocketR.emit("sendPrime", App.prefs.id+"9y6s0y9"+primeNum+ getSign(primeNum))
//    }
//
//    private val onReceiveR1 = Emitter.Listener {
//        try {
//            val msg = it[0].toString()
//            val r1 = msg.split("SiGn")[0].toBigInteger()
//            if(checkSign(msg, getPublicKey())){
//                val y = getY()
//                saveShardKey(r1, y)
//                sendR2(y)
//                registWard()
//            }else{
//                Log.e("서명 receiveR1", "서명 불일치")
//            }
//        }catch (e: Exception){
//            e.printStackTrace()
//        }
//    }
//
//    private fun getY(): BigInteger {
//        var y = BigInteger(1024, Random())
//        while(y > p.subtract(1.toBigInteger())){
//            y = BigInteger(1024, Random())
//        }
//        return y
//    }
//
//    private fun saveShardKey(r1: BigInteger, y: BigInteger){
//        val shardKey = r1.modPow(y, p)
//        App.prefs.shardKey = shardKey.toString()
//    }
//
//    private fun sendR2(y: BigInteger){
//        val r2 = g.modPow(y, p)
//        mSocketR.emit("sendR2", r2.toString()+ getSign(r2.toString()))
//    }
//
//    private fun registWard(){                  //피보호자 등록
//        val remoteID = this.remoteID
//        Singleton.server.registWard(App.prefs.id, remoteID).enqueue(object:Callback<ResponseDC>{
//            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
//                App.prefs.regKey = true
//                App.prefs.role = "Ward"
//                App.prefs.room = remoteID
//                startForeService()
//            }
//            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
//                Log.d("피보호자 등록", "실패")
//                Toast.makeText(context, "보호자 id가 존재하지 않거나\n이미 등록된 사용자입니다.", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }

    sealed interface GuardRegisterUiState {
        data object QRScanning: GuardRegisterUiState
        data object Loading: GuardRegisterUiState
        data object Failed: GuardRegisterUiState
        data object Connected: GuardRegisterUiState
    }
}