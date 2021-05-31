package com.myproject.safealarm

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.common.util.Base64Utils
import io.socket.client.IO
import okhttp3.OkHttpClient
import java.io.*
import java.lang.RuntimeException
import java.security.*
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class App: Application() {
    companion object{
        lateinit var prefs : PreferenceManager
        const val CNG_LOC = "CHANGE_LOCATION"
        val opts = set()
        val mSocket = IO.socket(MyAddress.url, opts)

        fun connectSocket(){
            try{
                mSocket.connect()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        fun getOkHttpClient(): OkHttpClient{
            try{
                val trustAllCert = arrayOf<TrustManager>(object : X509TrustManager{
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    }

                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                })
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCert, java.security.SecureRandom())
                val sslSocketFactory = sslContext.socketFactory

                val okHttpClient = OkHttpClient.Builder()
                    .hostnameVerifier{_, _ -> true}
                    .sslSocketFactory(sslSocketFactory, trustAllCert[0] as X509TrustManager)

                return okHttpClient.build()
            }catch(e: java.lang.Exception){
                throw RuntimeException(e)
            }
        }

        fun set(): IO.Options{
            val opts = IO.Options()
            var okHttpClient = getOkHttpClient()
            IO.setDefaultOkHttpWebSocketFactory(okHttpClient)
            IO.setDefaultOkHttpCallFactory(okHttpClient)
            opts.callFactory = okHttpClient
            opts.webSocketFactory = okHttpClient

            return opts
        }
    }
    override fun onCreate(){
        prefs = PreferenceManager(applicationContext)
        super.onCreate()
    }
}

object MyAddress{
    const val PERMISSIONS_REQUEST_CODE = 100
    const val url = "your_Server_IP_address"

}