package com.mandiri.newsapp.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.mandiri.newsapp.data.remote.di.NetworkModule
import com.mandiri.newsapp.paging.HeadlinesPagingSource
import com.mandiri.newsapp.paging.EverythingPagingSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest

class NewsViewModel : ViewModel() {

    private val api = NetworkModule.api

    // ========= Headlines by category =========
    private val _headlineCategory = MutableStateFlow<String?>(null)
    val headlineCategory: StateFlow<String?> = _headlineCategory.asStateFlow()

    val headlinePager = headlineCategory
        .debounce(150)
        .distinctUntilChanged()
        .flatMapLatest { cat ->
            Pager(
                config = PagingConfig(
                    pageSize = 10,
                    // FIX: prefetchDistance harus > 0 ATAU enablePlaceholders = true
                    prefetchDistance = 2,
                    initialLoadSize = 10,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { HeadlinesPagingSource(api, cat) }
            ).flow
        }
        .cachedIn(viewModelScope)

    fun setHeadlineCategory(category: String?) { _headlineCategory.value = category }

    // ========= Everything by query =========
    private val _query = MutableStateFlow("indonesia")
    val everythingPager = _query
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { q ->
            Pager(
                config = PagingConfig(
                    pageSize = 10,
                    // FIX sama seperti di atas
                    prefetchDistance = 2,
                    initialLoadSize = 10,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { EverythingPagingSource(api, q) }
            ).flow
        }
        .cachedIn(viewModelScope)

    fun setQuery(q: String) { _query.value = q }
}
