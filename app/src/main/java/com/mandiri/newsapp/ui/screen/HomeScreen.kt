package com.mandiri.newsapp.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.mandiri.newsapp.R
import com.mandiri.newsapp.data.remote.model.Article
import com.mandiri.newsapp.ui.components.ArticleListItem
import com.mandiri.newsapp.ui.components.GlassNavigationBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: NewsViewModel) {
    val ctx = LocalContext.current
    var currentTab by remember { mutableStateOf(0) }

    val categories = listOf(
        null to "All news",
        "business" to "Business",
        "technology" to "Tech",
        "science" to "Science"
    )

    // State default awal
    LaunchedEffect(Unit) {
        vm.setHeadlineCategory(null)
        vm.setQuery("indonesia")
    }

    // State dari VM
    val selectedCategory by vm.headlineCategory.collectAsState()
    val headlines = vm.headlinePager.collectAsLazyPagingItems()
    val everything = vm.everythingPager.collectAsLazyPagingItems()

    Scaffold(
        topBar = { BrandTopAppBar() },
        // ⛔ Tidak pakai bottomBar di Scaffold agar tidak menumpuk
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->

        // Hitung inset bawah sistem (gesture/nav bar)
        val sysBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // ===== Konten utama =====
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 12.dp + 72.dp + sysBottom // ruang untuk nav overlay
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tabs kategori
                item {
                    CategoryTabs(
                        items = categories.map { it.second },
                        selectedIndex = categories.indexOfFirst { it.first == selectedCategory }
                            .coerceAtLeast(0),
                        onSelect = { idx ->
                            val cat = categories[idx].first
                            vm.setHeadlineCategory(cat)
                            val q = when (cat) {
                                "business" -> "bisnis"
                                "technology" -> "teknologi"
                                "science" -> "sains"
                                else -> "indonesia"
                            }
                            vm.setQuery(q)
                        }
                    )
                }

                // Carousel headlines
                item {
                    HomeHeadlinesCarousel(
                        headlines = headlines,
                        everything = everything,
                        onOpen = { url ->
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    )
                }

                // Section title
                item {
                    Text(
                        "Latest news",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                // State awal feed
                item {
                    when (val s = everything.loadState.refresh) {
                        is LoadState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                        is LoadState.Error -> {
                            Column {
                                Text("Gagal memuat: ${s.error.localizedMessage ?: "-"}")
                                Spacer(Modifier.height(8.dp))
                                Button(onClick = { everything.retry() }) { Text("Coba lagi") }
                            }
                        }
                        else -> Unit
                    }
                }

                // Feed vertikal
                items(count = everything.itemCount, key = { i -> everything[i]?.url ?: i }) { i ->
                    val a = everything[i] ?: return@items
                    ArticleListItem(
                        article = a,
                        onClick = { url ->
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Footer paging
                item {
                    when {
                        everything.loadState.append is LoadState.Loading ->
                            LinearProgressIndicator(Modifier.fillMaxWidth())

                        everything.loadState.append is LoadState.Error -> {
                            val err = everything.loadState.append as LoadState.Error
                            Column {
                                Text("Gagal memuat lagi: ${err.error.localizedMessage ?: "-"}")
                                Spacer(Modifier.height(8.dp))
                                Button(onClick = { everything.retry() }) { Text("Coba lagi") }
                            }
                        }
                    }
                }
            }

            // ===== Placeholder tab lain (di bawah nav, tidak menutupi) =====
            if (currentTab != 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(0.5f), // tetap di bawah nav
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tab belum diimplementasi", style = MaterialTheme.typography.titleMedium)
                }
            }

            // ===== Glass Navigation Bar (overlay paling atas) =====
            GlassNavigationBar(
                current = currentTab,
                onChange = { currentTab = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 10.dp + sysBottom
                    )
                    .zIndex(2f) // pastikan di atas semua layer
            )
        }
    }
}

/* ===================== HOME CAROUSEL (anti-flicker) ===================== */

@Composable
private fun HomeHeadlinesCarousel(
    headlines: LazyPagingItems<Article>,
    everything: LazyPagingItems<Article>,
    onOpen: (String) -> Unit
) {
    fun snapshot(items: LazyPagingItems<Article>, max: Int): List<Article> =
        items.itemSnapshotList.items.filterNotNull().take(max)

    var triedHeadlines by remember { mutableStateOf(false) }
    LaunchedEffect(headlines.loadState.refresh) {
        if (!triedHeadlines &&
            headlines.loadState.refresh is LoadState.NotLoading &&
            headlines.itemCount == 0
        ) {
            triedHeadlines = true
            headlines.refresh()
        }
    }
    var triedEverything by remember { mutableStateOf(false) }
    LaunchedEffect(everything.loadState.refresh) {
        if (!triedEverything &&
            everything.loadState.refresh is LoadState.NotLoading &&
            everything.itemCount == 0
        ) {
            triedEverything = true
            everything.refresh()
        }
    }

    when (val s = headlines.loadState.refresh) {
        is LoadState.Loading -> { HomeCarouselCardSkeleton(); return }
        is LoadState.Error -> {
            val snap = snapshot(everything, 5)
            if (snap.isNotEmpty()) { HomePager(snap, onOpen); return }
            Column(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp),
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

    val snap = snapshot(headlines, 5)
    if (snap.isEmpty()) {
        val fb = snapshot(everything, 5)
        if (fb.isEmpty()) { HomeCarouselCardSkeleton(); return }
        HomePager(fb, onOpen); return
    }
    HomePager(snap, onOpen)
}

@Composable
private fun HomePager(
    data: List<Article>,
    onOpen: (String) -> Unit
) {
    val count = data.size
    val pagerState = rememberPagerState { count }
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val a = data[page]
            HomeCarouselCard(article = a, onClick = onOpen, modifier = Modifier.fillMaxWidth())
        }

        // Dots
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(count) { i ->
                val selected = pagerState.currentPage == i
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

        // Auto-slide stabil (1 coroutine loop)
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

/* ===================== Kartu Carousel ===================== */

@Composable
private fun HomeCarouselCard(
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
            AsyncImage(
                model = article.urlToImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xAA000000))
                        )
                    )
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = article.title.orEmpty().ifBlank { "(Untitled)" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
                        Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = formatRelativeOrEmpty(article.publishedAt),
                        style = MaterialTheme.typography.labelMedium.copy(color = Color.White)
                    )
                }
            }
        }
    }
}

/* ===================== Skeleton Card (TOP-LEVEL!) ===================== */

@Composable
private fun HomeCarouselCardSkeleton() {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}

/* ===================== Brand Top Bar ===================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrandTopAppBar() {
    TopAppBar(
        title = { BrandTitle() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun BrandTitle() {
    val bigLogoSize: Dp = 110.dp
    val anchorWidth: Dp = 56.dp
    val overlapToLeft: Dp = (-1).dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .width(anchorWidth)
                .height(anchorWidth),
            contentAlignment = Alignment.CenterStart
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_mandiri_logo),
                contentDescription = "Mandiri",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(bigLogoSize)
                    .offset(x = overlapToLeft)
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = "Mandiri News",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
                letterSpacing = 0.1.sp
            )
        )
    }
}

/* ===================== Tabs ===================== */

@Composable
private fun CategoryTabs(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    var selected by remember { mutableStateOf(selectedIndex) }
    LaunchedEffect(selectedIndex) { selected = selectedIndex }

    ScrollableTabRow(
        selectedTabIndex = selected,
        edgePadding = 0.dp,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = { positions ->
            TabRowDefaults.Indicator(
                Modifier
                    .tabIndicatorOffset(positions[selected])
                    .height(3.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        items.forEachIndexed { idx, label ->
            Tab(
                selected = selected == idx,
                onClick = { selected = idx; onSelect(idx) },
                text = {
                    Text(
                        label,
                        style = if (selected == idx)
                            MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        else
                            MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                    )
                }
            )
        }
    }
}

/* ===================== Util ===================== */

private fun formatRelativeOrEmpty(iso: String?): String =
    runCatching { iso?.take(10).orEmpty() }.getOrDefault("")
