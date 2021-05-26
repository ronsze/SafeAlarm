package com.myproject.safealarm

import android.content.Context
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.lang.RuntimeException
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.SocketFactory
import javax.net.ssl.*

data class ResponseDC(var result: String? = null)
data class ResponseInfo(var result: Any? = null,
                        var photo: Any? = null)
data class ResponseMissing(
                           val id: String? = null,
                           val name: String? = null,
                           val age: String? = null,
                           val sex: String? = null,
                           val height: String? = null,
                           val phone: String? = null,
                           val extra: String? = null,
                           val looks: String? = null,
                           val extra2: String? = null,
                           val loc: String? = null)

interface APIInterface{
    @FormUrlEncoded
    @POST("/db/create")
    fun fConnect(@Field("id")id:String):Call<ResponseDC>

    @FormUrlEncoded
    @POST("/regist/guard")
    fun registGuard(@Field("id")id: String): Call<ResponseDC>

    @FormUrlEncoded
    @POST("/regist/ward")
    fun registWard(@Field("id")id: String,
                   @Field("code")code: String): Call<ResponseDC>

    @FormUrlEncoded
    @POST("/db/missingInfo")
    fun postMissingInfo(@Field("id")id: String,
                        @Field("info")info: JSONObject): Call<ResponseDC>

    @Multipart
    @POST("/db/missingPhoto")
    fun postPhoto(@Part photo: MultipartBody.Part): Call<ResponseDC>

    @FormUrlEncoded
    @POST("/ca/createCert")
    fun postCSR(@Field("id")id: String,
                @Field("CSR")CSR: String): Call<ResponseDC>

    @GET("/ca/getCert")
    fun getCert(@Query("id")id: String): Call<ResponseDC>

    @GET("/db/getMissingInfo")
    fun getInfo(): Call<ResponseInfo>
}

object Singleton{
    val url = MyAddress.url
    val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .client(getUnsafeOkHttpClient().build())
        .build()
    var server = retrofit.create(APIInterface::class.java)

    fun getUnsafeOkHttpClient(): OkHttpClient.Builder{
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

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCert[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }

            return builder
        }catch(e: Exception){
            throw RuntimeException(e)
        }
    }
}