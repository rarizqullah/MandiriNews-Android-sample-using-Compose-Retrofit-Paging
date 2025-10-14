package com.mandiri.newsapp.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.mandiri.newsapp.data.remote.di.NetworkModule
import com.mandiri.newsapp.paging.HeadlinesPagingSource
import com.mandiri.newsapp.paging.EverythingPagingSource
import kotlinx.coroutines.flow.*

class NewsViewModel : ViewModel() {

    private val api = NetworkModule.api


    private val _headlineCategory = MutableStateFlow<String?>(null)
    val headlineCategory = _headlineCategory.asStateFlow()

    val headlinePager = headlineCategory
        .debounce(150)
        .distinctUntilChanged()
        .flatMapLatest { cat ->
            Pager(
                config = PagingConfig(pageSize = 20, prefetchDistance = 5),
                pagingSourceFactory = { HeadlinesPagingSource(api, cat) }
            ).flow
        }
        .cachedIn(viewModelScope)

    fun setHeadlineCategory(category: String?) { _headlineCategory.value = category }


    private val _query = MutableStateFlow("indonesia")
    val everythingPager = _query
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { q ->
            Pager(
                config = PagingConfig(pageSize = 20, prefetchDistance = 5),
                pagingSourceFactory = { EverythingPagingSource(api, q) }
            ).flow
        }
        .cachedIn(viewModelScope)

    fun setQuery(q: String) { _query.value = q }
}
