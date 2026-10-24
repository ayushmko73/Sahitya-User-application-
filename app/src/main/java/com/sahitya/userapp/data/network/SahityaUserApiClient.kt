package com.sahitya.userapp.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object SahityaUserApiClient {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun create(baseUrl: String): SahityaUserApiService {
        val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SahityaUserApiService::class.java)
    }
}
