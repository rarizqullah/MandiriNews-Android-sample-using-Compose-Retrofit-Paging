// NetworkModule.kt
package com.mandiri.newsapp.data.remote.di

import android.os.SystemClock
import com.mandiri.newsapp.BuildConfig
import com.mandiri.newsapp.data.remote.NewsApi
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private const val BASE_URL = "https://newsapi.org/"

    /** Tambah API key ke setiap request */
    private val authInterceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("X-Api-Key", BuildConfig.NEWS_API_KEY)
            .build()
        chain.proceed(req)
    }

    /** Rate limiter global (single flight + jeda minimal) */
    private object RateLimitInterceptor : Interceptor {
        @Volatile private var lastCallAtMs: Long = 0L
        private val lock = Any()
        override fun intercept(chain: Interceptor.Chain): Response {
            synchronized(lock) {
                val now = SystemClock.elapsedRealtime()
                val elapsed = now - lastCallAtMs
                val wait = 1200L - elapsed               // naikkan jadi 1.2s
                if (wait > 0) Thread.sleep(wait)

                var resp = chain.proceed(chain.request())
                lastCallAtMs = SystemClock.elapsedRealtime()

                if (resp.code == 429) {
                    val retryAfterSec = resp.header("Retry-After")?.toLongOrNull() ?: 2L
                    resp.close()
                    Thread.sleep(retryAfterSec * 1000)
                    // Pastikan tetap hormati jeda
                    val elapsed2 = SystemClock.elapsedRealtime() - lastCallAtMs
                    val wait2 = 1200L - elapsed2
                    if (wait2 > 0) Thread.sleep(wait2)
                    resp = chain.proceed(chain.request())
                    lastCallAtMs = SystemClock.elapsedRealtime()
                }
                return resp
            }
        }
    }

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    // ⬇️ KUNCI: dispatcher 1 request global
    private val dispatcher = Dispatcher().apply {
        maxRequests = 1
        maxRequestsPerHost = 1
    }

    // Opsional: cache ringan 30 dtk utk response identik (mengurangi kuota)
    private val cache = Cache(directory = java.io.File("/data/data/${BuildConfig.APPLICATION_ID}/cache/http"),
        maxSize = 5L * 1024L * 1024L)

    private val client: OkHttpClient = OkHttpClient.Builder()
        .dispatcher(dispatcher)
        .cache(cache)
        .addInterceptor(authInterceptor)
        .addInterceptor(RateLimitInterceptor)
        .addInterceptor(logger)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: NewsApi = retrofit.create(NewsApi::class.java)
}
