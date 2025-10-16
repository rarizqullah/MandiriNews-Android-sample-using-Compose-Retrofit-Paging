package com.mandiri.newsapp.data.remote.model

data class Article(
    val source: Source?,
    val author: String?,
    val title: String?,        // boleh null
    val description: String?,
    val url: String?,          // boleh null
    val urlToImage: String?,
    val publishedAt: String?,  // boleh null
    val content: String?
)
