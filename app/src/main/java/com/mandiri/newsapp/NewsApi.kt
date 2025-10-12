package com.mandiri.newsapp

import com.mandiri.newsapp.data.remote.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    @GET("v2/top-headlines")
    suspend fun topHeadlines(
        @Query("country") country: String = "id",
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 20
    ): NewsResponse

    @GET("v2/everything")
    suspend fun everything(
        @Query("q") query: String,
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 20
    ): NewsResponse
}
