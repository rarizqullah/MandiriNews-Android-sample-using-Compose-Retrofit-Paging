package com.mandiri.newsapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mandiri.newsapp.data.remote.model.Article
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.min

@Composable
fun HeadlinesCarouselSmart(
    headlines: LazyPagingItems<Article>,
    everything: LazyPagingItems<Article>,
    onOpen: (String) -> Unit
) {
    when (val s = headlines.loadState.refresh) {
        is LoadState.Loading -> { CarouselCardSkeleton(); return }
        is LoadState.Error -> {
            if (everything.itemCount > 0) { FallbackCarouselFromEverything(everything, onOpen); return }
            Column(
                Modifier.fillMaxWidth().height(200.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Gagal memuat headline: ${s.error.localizedMessage ?: "-"}")
                Spacer(Modifier.height(8.dp))
                Button(onClick = { headlines.retry() }) { Text("Coba lagi") }
            }
            return
        }
        else -> Unit
    }

    if (headlines.itemCount == 0) {
        if (everything.itemCount == 0) { CarouselCardSkeleton(); return }
        FallbackCarouselFromEverything(everything, onOpen); return
    }

    val count = min(5, headlines.itemCount)
    val pagerState = rememberPagerState { count }
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val a = headlines[page]
            key(a?.url ?: page) {
                if (a == null) CarouselCardSkeleton()
                else CarouselCard(article = a, onClick = onOpen, modifier = Modifier.fillMaxWidth())
            }
        }
        Dots(count = count, current = pagerState.currentPage)

        LaunchedEffect(count) {
            if (count > 1) {
                while (isActive) {
                    delay(3500)
                    scope.launch {
                        val next = (pagerState.currentPage + 1) % count
                        pagerState.animateScrollToPage(next)
                    }
                }
            }
        }
    }
}

@Composable
fun FallbackCarouselFromEverything(
    everything: LazyPagingItems<Article>,
    onOpen: (String) -> Unit
) {
    val total = everything.itemCount
    val count = min(5, if (total > 0) total else 0)
    if (count == 0) { CarouselCardSkeleton(); return }

    val pagerState = rememberPagerState { count }
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val a = everything[page]
            key(a?.url ?: page) {
                if (a == null) CarouselCardSkeleton()
                else CarouselCard(article = a, onClick = onOpen, modifier = Modifier.fillMaxWidth())
            }
        }
        Dots(count = count, current = pagerState.currentPage)

        LaunchedEffect(count) {
            if (count > 1) {
                while (isActive) {
                    delay(3500)
                    scope.launch {
                        val next = (pagerState.currentPage + 1) % count
                        pagerState.animateScrollToPage(next)
                    }
                }
            }
        }
    }
}

@Composable
fun CarouselCard(
    article: Article,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val link = article.url
    ElevatedCard(
        onClick = { link?.let(onClick) },
        enabled = !link.isNullOrBlank(),
        modifier = modifier.height(200.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Box(Modifier.fillMaxSize()) {
            val request = ImageRequest.Builder(LocalContext.current)
                .data(article.urlToImage)
                .crossfade(true)
                .build()
            AsyncImage(
                model = request,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color(0xAA000000)))
                )
            )
            Column(Modifier.align(Alignment.BottomStart).padding(14.dp)) {
                Text(
                    text = article.title.orEmpty().ifBlank { "(Untitled)" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White, fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = article.source?.name.orEmpty(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier.size(4.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = formatRelative(article.publishedAt),
                        style = MaterialTheme.typography.labelMedium.copy(color = Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun CarouselCardSkeleton() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        shape = MaterialTheme.shapes.large
    ) { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) }
}
@Composable
private fun Dots(count: Int, current: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        repeat(count) { i ->
            val selected = current == i
            Box(
                Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (selected) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    )
            )
        }
    }
}

private fun formatRelative(iso: String?): String =
    runCatching { iso?.take(10).orEmpty() }.getOrDefault("-")
