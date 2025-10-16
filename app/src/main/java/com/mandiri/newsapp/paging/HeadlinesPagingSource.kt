package com.mandiri.newsapp.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mandiri.newsapp.data.remote.NewsApi
import com.mandiri.newsapp.data.remote.model.Article

class HeadlinesPagingSource(
    private val api: NewsApi,
    private val category: String?
) : PagingSource<Int, Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> = try {
        val page = params.key ?: 1
        val resp = api.topHeadlines(
            country = "id",
            category = category,
            page = page,
            pageSize = params.loadSize.coerceAtMost(10)
        )

        val articles = (resp.articles ?: emptyList())
            .filter { !it.url.isNullOrBlank() }
        LoadResult.Page(
            data = articles,
            prevKey = if (page == 1) null else page - 1,
            nextKey = if (articles.isEmpty()) null else page + 1
        )
    } catch (t: Throwable) {
        LoadResult.Error(t)
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        val pos = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(pos)
        return page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
    }
}
