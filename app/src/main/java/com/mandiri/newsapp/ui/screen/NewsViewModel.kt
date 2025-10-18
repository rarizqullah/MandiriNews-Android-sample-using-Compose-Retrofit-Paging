package com.mandiri.newsapp.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.mandiri.newsapp.data.remote.di.NetworkModule
import com.mandiri.newsapp.paging.HeadlinesPagingSource
import com.mandiri.newsapp.paging.EverythingPagingSource
import com.mandiri.newsapp.data.remote.model.Article
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class NewsViewModel : ViewModel() {

    private val api = NetworkModule.api
    private val _headlineCategory = MutableStateFlow<String?>(null)
    val headlineCategory: StateFlow<String?> = _headlineCategory.asStateFlow()

    val headlinePager = headlineCategory
        .debounce(150)
        .distinctUntilChanged()
        .flatMapLatest { cat ->
            Pager(
                config = PagingConfig(
                    pageSize = 10,
                    prefetchDistance = 2,
                    initialLoadSize = 10,
                    enablePlaceholders = false
                ),
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
                config = PagingConfig(
                    pageSize = 10,
                    prefetchDistance = 2,
                    initialLoadSize = 10,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { EverythingPagingSource(api, q) }
            ).flow
        }
        .cachedIn(viewModelScope)

    fun setQuery(q: String) { _query.value = q }


    private val _bookmarks = MutableStateFlow<Map<String, Article>>(emptyMap())


    val bookmarks: StateFlow<List<Article>> =
        _bookmarks.map { it.values.toList() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun toggleBookmark(article: Article) {
        val key = article.url ?: return
        _bookmarks.update { cur ->
            if (cur.containsKey(key)) cur - key else cur + (key to article)
        }
    }

    fun isBookmarked(url: String?): Boolean =
        url != null && _bookmarks.value.containsKey(url)


    enum class Edition { US, INTERNATIONAL }

    private val _accountEmail = MutableStateFlow<String?>("azizrahmanxv@gmail.com")
    val accountEmail: StateFlow<String?> = _accountEmail.asStateFlow()

    private val _edition = MutableStateFlow(Edition.INTERNATIONAL)
    val edition: StateFlow<Edition> = _edition.asStateFlow()

    private val _cnnSoundEnabled = MutableStateFlow(false)
    val cnnSoundEnabled: StateFlow<Boolean> = _cnnSoundEnabled.asStateFlow()

    fun setEdition(e: Edition) { _edition.value = e }
    fun setCnnSoundEnabled(enabled: Boolean) { _cnnSoundEnabled.value = enabled }
    fun setAccountEmail(email: String?) { _accountEmail.value = email }

}
