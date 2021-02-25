package com.myproject.safealarm

import io.socket.client.IO
import io.socket.client.Socket
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
    fun missingInfoGet(@Field("id")id: Int): Call<ResponseDC>

    @FormUrlEncoded
    @POST("/db/missingInfo")
    fun missingInfoPost(@Field("photo")photo: String,
                        @Field("info")info: String,
                        @Field("time")time: String,
                        @Field("look")look: String,
                        @Field("other")other: String): Call<ResponseDC>
}

object Singleton{

    val url = MyAddress.url
    val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    var server = retrofit.create(APIInterface::class.java)
}