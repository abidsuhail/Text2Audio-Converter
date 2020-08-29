package com.dragontelnet.text2speech.retrofit

import com.dragontelnet.text2speech.api.Text2AudioApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitService {
    private const val BASE_URL = "https://voicerss-text-to-speech.p.rapidapi.com"
    fun getInstance(): Text2AudioApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(Text2AudioApi::class.java)
    }
}