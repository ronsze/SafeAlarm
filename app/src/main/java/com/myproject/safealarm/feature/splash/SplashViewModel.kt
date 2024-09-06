package com.myproject.safealarm.feature.splash

import androidx.lifecycle.viewModelScope
import com.myproject.safealarm.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.sdbk.domain.usecase.user_auth.GetUserUseCase
import kr.sdbk.domain.usecase.user_service.GetUserProfileUseCase
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
): BaseViewModel() {
    private val _uiState: MutableStateFlow<SplashUiState> = MutableStateFlow(SplashUiState.Loading)
    val uiState get() = _uiState.asStateFlow()

    fun checkUser() {
        getUserUseCase(
            scope = viewModelScope,
            onSuccess = { user ->
                if (user == null) _uiState.set(SplashUiState.LoggedOut)
                else checkUserState()
            },
            onFailure = {
                basicErrorHandling(it)
            }
        )
    }

    private fun checkUserState() {
        getUserProfileUseCase(
            scope = viewModelScope,
            onSuccess = { profile ->
                when {
                    profile.partnerId != null -> checkConnection()
                    profile.role != null -> _uiState.set(SplashUiState.NeedConnection)
                    profile.role == null -> _uiState.set(SplashUiState.RoleSelect)
                }
            },
            onFailure = {
                basicErrorHandling(it)
            }
        )
    }

    private fun checkConnection() {
        _uiState.set(SplashUiState.Connected)
    }

//    private fun saveCACertificate(certificate: String){
//        App.connectSocket()
//
//        Singleton.server.getCaCert().enqueue(object: Callback<ResponseDC>{
//            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
//                val certificate = response.body()!!.result!!
//                saveCACertificate(certificate)
//                checkPermission()
//            }
//
//            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
//                Log.e("인증서", "CA인증서 받기 실패")
//            }
//
//        })
//
//        var tempFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "caCert.crt")
//        try{
//            val writer = FileWriter(tempFile)
//            val buffer = BufferedWriter(writer)
//            buffer.write(certificate)
//            buffer.close()
//        }catch(e: java.lang.Exception){
//            e.printStackTrace()
//        }
//
//        var cf = CertificateFactory.getInstance("X.509")
//        var caIn = BufferedInputStream(FileInputStream(tempFile))
//        var ca = caIn.use{
//            cf.generateCertificate(it) as X509Certificate
//        }
//        var kf = KeyFactory.getInstance("RSA")
//        var public = kf.generatePublic(X509EncodedKeySpec(ca.publicKey.encoded))
//
//        App.prefs.CAPublic = Base64Utils.encode(public.encoded)
//    }
//
//    private fun registCertificate(){
//        val csr = createX509CSR()
//        Singleton.server.postCSR(App.prefs.id, csr).enqueue(object:Callback<ResponseDC>{
//            override fun onResponse(call: Call<ResponseDC>, response: Response<ResponseDC>) {
//                val intent = Intent(context, RegisterActivity::class.java)
//                App.prefs.csr = csr
//                startActivity(intent)
//                finish()
//
//            }
//            override fun onFailure(call: Call<ResponseDC>, t: Throwable) {
//                Log.e("CSR등록", "실패")
//            }
//        })
//    }

//    private fun createX509CSR(): String{
//        val keyPair = getRSAKeyPair()
//
//        val sigAlg = "SHA256withRSA"
//        val params = "C=kr,O=SuwonUniv,CN=${App.prefs.id}"
//        val p10builder: PKCS10CertificationRequestBuilder = JcaPKCS10CertificationRequestBuilder(
//            X500Name(params), keyPair.public)
//        val csBuilder = JcaContentSignerBuilder(sigAlg)
//        csBuilder.setProvider(BouncyCastleProvider())
//        val signer = csBuilder.build(keyPair.private)
//        var p10 = p10builder.build(signer)
//
//        val pemObject = PemObject("CERTIFICATE REQUEST", p10.encoded)
//        var csr = StringWriter()
//        var jcaPEMWriter = JcaPEMWriter(csr)
//        jcaPEMWriter.writeObject(pemObject)
//        jcaPEMWriter.close()
//        csr.close()
//        return csr.toString()
//    }

//    private fun getRSAKeyPair(): KeyPair {
//        val keygen = KeyPairGenerator.getInstance("RSA")
//        keygen.initialize(2048, SecureRandom())
//        val keyPair = keygen.genKeyPair()
//        App.prefs.privateKey = Base64Utils.encode(keyPair.private.encoded)
//
//        return keyPair
//    }

    sealed interface SplashUiState {
        data object Loading: SplashUiState
        data object Connected: SplashUiState
        data object NeedConnection: SplashUiState
        data object RoleSelect: SplashUiState
        data object LoggedOut: SplashUiState
    }
}