package com.mandiri.newsapp.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mandiri.newsapp.data.remote.NewsApi
import com.mandiri.newsapp.data.remote.model.Article

class EverythingPagingSource(
    private val api: NewsApi,
    private val query: String
) : PagingSource<Int, Article>() {

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? =
        state.anchorPosition?.let { anchor ->
            val page = state.closestPageToPosition(anchor)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> = try {
        val page = params.key ?: 1
        val resp = api.everything(
            query = query,
            sortBy = "publishedAt",
            page = page,
            pageSize = params.loadSize.coerceAtMost(20),
            language = "id"
        )
        val data = (resp.articles ?: emptyList())
            .filter { !it.url.isNullOrBlank() }
        LoadResult.Page(
            data = data,
            prevKey = if (page == 1) null else page - 1,
            nextKey = if (data.isEmpty()) null else page + 1
        )
    } catch (e: Exception) {
        LoadResult.Error(e)
    }
}
