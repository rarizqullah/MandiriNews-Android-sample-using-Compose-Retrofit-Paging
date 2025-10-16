package com.mandiri.newsapp.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.mandiri.newsapp.data.remote.model.Article
import com.mandiri.newsapp.ui.components.ArticleCard

@Composable
fun HeadlineSection(
    items: LazyPagingItems<Article>,
    selected: String?,
    onChangeCategory: (String?) -> Unit
) {
    val ctx = LocalContext.current
    val categories = remember {
        listOf(
            null to "Semua",
            "business" to "Bisnis",
            "technology" to "Teknologi",
            "entertainment" to "Hiburan",
            "sports" to "Olahraga",
            "health" to "Kesehatan",
            "science" to "Sains"
        )
    }

    Column(Modifier.fillMaxSize()) {

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories.size) { i ->
                val (value, label) = categories[i]
                FilterChip(
                    selected = selected == value,
                    onClick = { onChangeCategory(value) },
                    label = { Text(label) }
                )
            }
        }

        PagingStateHeader(items)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(count = items.itemCount) { index ->
                val article = items[index] ?: return@items
                ArticleCard(
                    article = article,
                    onClick = { url ->
                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                )
            }
            item { PagingStateFooter(items) }
        }
    }
}

@Composable
private fun PagingStateHeader(items: LazyPagingItems<*>) {
    when (val s = items.loadState.refresh) {
        is LoadState.Loading -> {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        is LoadState.Error -> {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("Gagal memuat: ${s.error.localizedMessage ?: "-"}")
                Spacer(Modifier.height(6.dp))
                Button(onClick = { items.retry() }) { Text("Coba lagi") }
            }
        }
        else -> {
            if (items.itemCount == 0) {
                Text(
                    "Belum ada berita untuk filter ini.",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PagingStateFooter(items: LazyPagingItems<*>) {
    when {
        items.loadState.append is LoadState.Loading -> {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        items.loadState.append is LoadState.Error -> {
            val err = items.loadState.append as LoadState.Error
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("Gagal memuat lagi: ${err.error.localizedMessage ?: "-"}")
                Spacer(Modifier.height(6.dp))
                Button(onClick = { items.retry() }) { Text("Coba lagi") }
            }
        }
    }
}
