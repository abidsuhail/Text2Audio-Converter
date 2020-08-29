package com.dragontelnet.text2speech.api

import com.dragontelnet.text2speech.BuildConfig
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface Text2AudioApi {


    @FormUrlEncoded
    @Headers(
        "x-rapidapi-host:voicerss-text-to-speech.p.rapidapi.com",
        "x-rapidapi-key:${BuildConfig.RAPID_API_KEY}",
        "content-type:application/x-www-form-urlencoded",
        "useQueryString:true"
    )
    @POST("/")
    fun get(@FieldMap map: Map<String, String>, @Query("key") key: String): Call<ResponseBody>
}