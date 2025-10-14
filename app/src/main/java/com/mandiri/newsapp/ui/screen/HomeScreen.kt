package com.mandiri.newsapp.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.mandiri.newsapp.data.remote.model.Article
import com.mandiri.newsapp.ui.components.ArticleCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: NewsViewModel) {
    var tab by remember { mutableStateOf(0) }

    val headlines = vm.headlinePager.collectAsLazyPagingItems()
    val everything = vm.everythingPager.collectAsLazyPagingItems()
    val selectedCategory by vm.headlineCategory.collectAsState(initial = null)

    Scaffold(topBar = { TopAppBar(title = { Text("NewsApp - Bank Mandiri") }) }) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            TabRow(
                selectedTabIndex = tab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[tab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 2.dp
                    )
                }
            ) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Headlines") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("All News") })
            }
            Spacer(Modifier.height(8.dp))

            if (tab == 0) {
                HeadlineSection(
                    items = headlines,
                    selected = selectedCategory,
                    onChangeCategory = { category -> vm.setHeadlineCategory(category) }
                )
            } else {
                SearchBar(vm = vm)
                NewsList(items = everything)
            }
        }
    }
}

@Composable
private fun SearchBar(vm: NewsViewModel) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            placeholder = { Text("Cari berita") },
            singleLine = true,
            shape = CircleShape
        )

        Button(
            onClick = { vm.setQuery(text) },
            modifier = Modifier.height(48.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 18.dp)
        ) {
            Text("Cari")
        }
    }
}

@Composable
private fun NewsList(items: LazyPagingItems<Article>) {
    val ctx = LocalContext.current

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

@Composable
private fun PagingStateFooter(items: LazyPagingItems<*>) {
    when {
        items.loadState.append is LoadState.Loading -> {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
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
