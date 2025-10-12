package com.mandiri.newsapp.data.remote.di

import com.mandiri.newsapp.BuildConfig
import com.mandiri.newsapp.data.remote.NewsApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    private const val BASE_URL = "https://newsapi.org/"

    private val authInterceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("X-Api-Key", BuildConfig.NEWS_API_KEY) // << WAJIB
            .build()
        chain.proceed(req)
    }

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logger)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL) // << harus https dan diakhiri /
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: NewsApi = retrofit.create(NewsApi::class.java)
}
