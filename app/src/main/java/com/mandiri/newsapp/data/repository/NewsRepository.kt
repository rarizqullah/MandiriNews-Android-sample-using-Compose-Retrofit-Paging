package com.mandiri.newsapp.data.repository

import com.mandiri.newsapp.data.remote.NewsApi

class NewsRepository(private val api: NewsApi) {
    suspend fun topHeadlines(country: String, category: String?, page: Int, pageSize: Int) =
        api.topHeadlines(country, category, page, pageSize)

    suspend fun everything(query: String, page: Int, pageSize: Int) =
        api.everything(query = query, page = page, pageSize = pageSize)
}
