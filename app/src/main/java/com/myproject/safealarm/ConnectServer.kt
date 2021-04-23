package com.myproject.safealarm

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class ResponseDC(var result: String? = null)
data class ResponseMissing(val photo: String? = null,
                           val info: String? = null,
                           val time: String? = null,
                           val look: String? = null,
                           val other: String? = null)

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
    @GET("/db/missingInfo")
    fun missingInfoGet(@Field("id")id: Int): Call<ResponseMissing>

    @Multipart
    @POST("/db/missingInfo")
    fun postMissingInfo(@Part("id")id: String,
                        @Part photo: MultipartBody,
                        @Part("info")info: String,
                        @Part("time")time: String,
                        @Part("look")look: String,
                        @Part("other")other: String): Call<ResponseDC>
}

object Singleton{
    val url = MyAddress.url
    val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    var server = retrofit.create(APIInterface::class.java)
}