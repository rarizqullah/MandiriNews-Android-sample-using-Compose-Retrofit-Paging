package com.mandiri.newsapp.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mandiri.newsapp.data.remote.NewsApi
import com.mandiri.newsapp.data.remote.model.Article

class HeadlinesPagingSource(
    private val api: NewsApi,
    private val category: String?
) : PagingSource<Int, Article>() {

    private fun qFor(cat: String?): String = when (cat) {
        "business"      -> "bisnis OR ekonomi OR saham OR keuangan"
        "technology"    -> "teknologi OR gadget OR startup OR AI OR aplikasi"
        "entertainment" -> "hiburan OR selebriti OR film OR musik"
        "sports"        -> "olahraga OR sepak bola OR badminton OR basket"
        "health"        -> "kesehatan OR rumah sakit OR penyakit OR vaksin"
        "science"       -> "sains OR penelitian OR ilmiah"
        else            -> "indonesia"
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        return try {
            val page = params.key ?: 1
            val resp = api.everything(
                query = qFor(category),
                sortBy = "publishedAt",
                page = page,
                pageSize = params.loadSize.coerceAtMost(20),
                language = "id"
            )
            val articles = resp.articles
            val nextKey = if (articles.isEmpty()) null else page + 1
            LoadResult.Page(
                data = articles,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            )
        } catch (t: Throwable) {
            LoadResult.Error(t)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        val pos = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(pos)
        return page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
    }
}
