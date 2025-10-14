package com.mandiri.newsapp.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
    val cs = MaterialTheme.colorScheme
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = cs.surface,
        labelColor = cs.onSurface,
        iconColor = cs.onSurface,
        selectedContainerColor = cs.primary,
        selectedLabelColor = cs.onPrimary,
        selectedLeadingIconColor = cs.onPrimary
    )

    Column(Modifier.fillMaxSize()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories.size) { i ->
                val (value, label) = categories[i]
                val isSelected = selected == value
                FilterChip(
                    selected = isSelected,
                    onClick = { onChangeCategory(value) },
                    label = { Text(label) },
                    shape = CircleShape,
                    colors = chipColors,
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) cs.primary else cs.outline,
                        selectedBorderColor = cs.primary
                    )
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
