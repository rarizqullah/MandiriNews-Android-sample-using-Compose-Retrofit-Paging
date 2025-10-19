package com.mandiri.newsapp.data.remote

import com.mandiri.newsapp.data.remote.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("v2/top-headlines")
    suspend fun topHeadlines(
        @Query("country") country: String = "id",
        @Query("category") category: String? = null,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 20
    ): NewsResponse

    @GET("v2/everything")
    suspend fun everything(
        @Query("q") query: String,
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 20,
        @Query("language") language: String? = null
    ): NewsResponse
}
