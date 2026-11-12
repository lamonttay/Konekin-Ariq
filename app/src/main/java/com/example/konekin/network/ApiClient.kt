package com.example.konekin.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    fun getInstance(): ApiService {
        val mHttpLoggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

        val mOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(mHttpLoggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .build()
                chain.proceed(request)
            }
            .build()

        val builder = Retrofit.Builder()
            .baseUrl("https://dummy.restapiexample.com/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(mOkHttpClient)
            .build()

        return builder.create(ApiService::class.java)
    }
}