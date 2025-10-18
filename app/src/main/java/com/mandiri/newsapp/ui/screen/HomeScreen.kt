package com.mandiri.newsapp.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
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
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.mandiri.newsapp.R
import com.mandiri.newsapp.data.remote.di.NetworkModule
import com.mandiri.newsapp.data.remote.model.Article
import com.mandiri.newsapp.paging.EverythingPagingSource
import com.mandiri.newsapp.ui.components.ArticleListItem
import com.mandiri.newsapp.ui.components.GlassNavigationBar
import com.mandiri.newsapp.ui.theme.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


private enum class Edition { NASIONAL, INTERNATIONAL }


private fun buildQuery(cat: String?, edition: Edition): String {

    val catQ = when (cat) {
        "business"    -> "(ekonomi OR bisnis OR finance OR business)"
        "technology"  -> "(teknologi OR technology OR tech)"
        "science"     -> "(sains OR science OR riset OR research)"
        else          -> "(ekonomi OR bisnis OR technology OR tech OR sains OR science)"
    }
    return when (edition) {
        Edition.NASIONAL      -> "$catQ AND indonesia"
        Edition.INTERNATIONAL -> catQ
    }
}


private fun fallbackQuery(cat: String?, edition: Edition): String {
    val catQ = when (cat) {
        "business"    -> "(business OR economy)"
        "technology"  -> "(technology OR tech)"
        "science"     -> "(science OR research)"
        else          -> "(business OR technology OR science)"
    }
    return when (edition) {
        Edition.NASIONAL      -> "$catQ AND indonesia"
        Edition.INTERNATIONAL -> catQ
    }
}


private fun videoQuery(topic: String, edition: Edition): String {
    val base = when (topic) {
        "politics" -> "(politik OR politics)"
        "world"    -> "(world OR international)"
        else       -> "(business OR economy)"
    }
    val videoHints = "(video OR clip OR interview)"
    val scope = if (edition == Edition.NASIONAL) " AND indonesia" else ""
    return "$base AND $videoHints$scope"
}

private fun watchPagerFlow(query: String): Flow<androidx.paging.PagingData<Article>> {
    val api = NetworkModule.api
    return Pager(
        config = PagingConfig(pageSize = 10, prefetchDistance = 2, initialLoadSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { EverythingPagingSource(api, query) }
    ).flow
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: NewsViewModel,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val ctx = LocalContext.current
    var currentTab by remember { mutableStateOf(0) }

    val categories = listOf(
        null to "All news",
        "business" to "Business",
        "technology" to "Tech",
        "science" to "Science"
    )

    val savedMap = remember { mutableStateMapOf<String, Article>() }
    fun isBookmarked(url: String?) = !url.isNullOrBlank() && savedMap.containsKey(url)
    fun toggleBookmark(a: Article) {
        val k = a.url ?: return
        if (savedMap.containsKey(k)) savedMap.remove(k) else savedMap[k] = a
    }
    val savedList = remember(savedMap.keys) { savedMap.values.toList() }

    var edition by remember { mutableStateOf(Edition.INTERNATIONAL) }
    val infoMode = when (themeMode) {
        ThemeMode.DARK -> "dark"
        ThemeMode.LIGHT -> "light"
        else -> "system"
    }

    val selectedCategory by vm.headlineCategory.collectAsState()
    val headlines = vm.headlinePager.collectAsLazyPagingItems()
    val everything = vm.everythingPager.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        vm.setHeadlineCategory(null)
        vm.setQuery(buildQuery(null, edition))
    }
    LaunchedEffect(edition, selectedCategory) {
        vm.setQuery(buildQuery(selectedCategory, edition))
    }
    var triedFallback by remember { mutableStateOf(false) }
    LaunchedEffect(everything.loadState.refresh, everything.itemCount, edition, selectedCategory) {
        val notLoading = everything.loadState.refresh is LoadState.NotLoading
        if (notLoading && everything.itemCount == 0 && !triedFallback) {
            triedFallback = true
            vm.setQuery(fallbackQuery(selectedCategory, edition))
        }
        if (!notLoading) triedFallback = false
    }

    Scaffold(
        topBar = { BrandTopAppBar() },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->

        val sysBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            when (currentTab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(0f),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 12.dp,
                            bottom = 12.dp + 72.dp + sysBottom
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            CategoryTabs(
                                items = categories.map { it.second },
                                selectedIndex = categories.indexOfFirst { it.first == selectedCategory }
                                    .coerceAtLeast(0),
                                onSelect = { idx ->
                                    val cat = categories[idx].first
                                    vm.setHeadlineCategory(cat)
                                    vm.setQuery(buildQuery(cat, edition))
                                    triedFallback = false
                                }
                            )
                        }
                        item {
                            HomeHeadlinesCarousel(
                                headlines = headlines,
                                everything = everything,
                                onOpen = { url ->
                                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                            )
                        }
                        item {
                            Text(
                                "Latest news",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
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
                        items(count = everything.itemCount, key = { i -> everything[i]?.url ?: i }) { i ->
                            val a = everything[i] ?: return@items
                            ArticleListItem(
                                article = a,
                                onClick = { url ->
                                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                isBookmarked = isBookmarked(a.url),
                                onToggleBookmark = { toggleBookmark(it) }
                            )
                        }
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
                }

                1 -> {
                    val politicsWatch = remember(edition) {
                        watchPagerFlow(videoQuery("politics", edition))
                    }.collectAsLazyPagingItems()

                    val worldWatch = remember(edition) {
                        watchPagerFlow(videoQuery("world", edition))
                    }.collectAsLazyPagingItems()

                    val businessWatch = remember(edition) {
                        watchPagerFlow(videoQuery("business", edition))
                    }.collectAsLazyPagingItems()

                    WatchScreen(
                        politics = politicsWatch,
                        world = worldWatch,
                        business = businessWatch,
                        onOpen = { url -> ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp, bottom = 12.dp + 72.dp + sysBottom)
                    )
                }

                2 -> {
                    SavedScreen(
                        items = savedList,
                        onOpen = { url -> ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
                        onToggle = { toggleBookmark(it) },
                        isBookmarked = { url -> isBookmarked(url) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 72.dp + sysBottom)
                    )
                }
                3 -> {
                    SettingsScreenCompact(
                        themeMode = themeMode,
                        infoMode = infoMode,
                        edition = edition,
                        onEditionChange = { edition = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 12.dp,
                                bottom = 12.dp + 72.dp + sysBottom
                            )
                    )
                }
            }

            // Bottom glass nav
            GlassNavigationBar(
                current = currentTab,
                onChange = { currentTab = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 12.dp, end = 12.dp, bottom = 10.dp + sysBottom)
                    .zIndex(2f)
            )
        }
    }
}

@Composable
private fun WatchScreen(
    politics: LazyPagingItems<Article>,
    world: LazyPagingItems<Article>,
    business: LazyPagingItems<Article>,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    fun snap(items: LazyPagingItems<Article>, max: Int) =
        items.itemSnapshotList.items.filterNotNull().take(max)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Watch", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.SemiBold)

        WatchSection(title = "Politics", data = snap(politics, 8), onOpen = onOpen)
        WatchSection(title = "World News", data = snap(world, 8), onOpen = onOpen)
        WatchSection(title = "Business", data = snap(business, 8), onOpen = onOpen)

        if (politics.loadState.refresh is LoadState.Loading ||
            world.loadState.refresh is LoadState.Loading ||
            business.loadState.refresh is LoadState.Loading
        ) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun WatchSection(
    title: String,
    data: List<Article>,
    onOpen: (String) -> Unit
) {
    if (data.isEmpty()) return
    Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
    val rows = if (data.size % 2 == 0) data.size / 2 else data.size / 2 + 1
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(rows) { r ->
            val left = data.getOrNull(r * 2)
            val right = data.getOrNull(r * 2 + 1)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WatchCard(article = left, onOpen = onOpen, modifier = Modifier.weight(1f))
                WatchCard(article = right, onOpen = onOpen, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WatchCard(
    article: Article?,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (article == null) {
        Spacer(modifier = modifier.height(0.dp)); return
    }
    val link = article.url
    Column(modifier = modifier.clickable(enabled = !link.isNullOrBlank()) { link?.let(onOpen) }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = article.urlToImage,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0x33000000))
                        )
                    )
            )
            Text("▶", color = Color.White, modifier = Modifier.align(Alignment.TopStart).padding(6.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            article.title.orEmpty().ifBlank { "(Untitled)" },
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

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
    val bigLogoSize: Dp = 140.dp
    val anchorWidth: Dp = 72.dp
    val overlapToLeft: Dp = (-4).dp

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
        Spacer(Modifier.width(6.dp))
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

@Composable
private fun SavedScreen(
    items: List<Article>,
    onOpen: (String) -> Unit,
    onToggle: (Article) -> Unit,
    isBookmarked: (String?) -> Boolean,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Belum ada artikel tersimpan", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Ketuk ikon bookmark pada artikel untuk menyimpan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(count = items.size, key = { i -> items[i].url ?: i }) { i ->
            val a = items[i]
            ArticleListItem(
                article = a,
                onClick = { url -> onOpen(url) },
                isBookmarked = isBookmarked(a.url),
                onToggleBookmark = onToggle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SettingsScreenCompact(
    themeMode: ThemeMode,
    infoMode: String,
    edition: Edition,
    onEditionChange: (Edition) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(
            "Settings",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        SectionHeader(text = "ACCOUNT")
        NavRow(title = "Account Settings", trailingText = "rafirizqullah4@gmail.com") { }

        SpacerDivider()

        SectionHeader(text = "APP PREFERENCES")
        Text("Editions", style = MaterialTheme.typography.bodySmall, modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp))
        SegmentedTwoOptions(
            left = "Nasional",
            right = "International",
            selectedRight = (edition == Edition.INTERNATIONAL),
            onSelectLeft = { onEditionChange(Edition.NASIONAL) },
            onSelectRight = { onEditionChange(Edition.INTERNATIONAL) },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "You are currently viewing Mandiri News in $infoMode mode.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        SpacerDivider()

        SectionHeader(text = "GENERAL")
        NavRow(title = "Help with subscriptions") {}
        NavRow(title = "Help for closed captioning") {}
        NavRow(title = "Privacy Policy") {}
        NavRow(title = "Terms of Use") {}
        NavRow(title = "Ad choices") {}
        NavRow(title = "Report an ad") {}
        NavRow(title = "App feedback") {}

        val versionName = try { com.mandiri.newsapp.BuildConfig.VERSION_NAME } catch (_: Throwable) { "-" }
        val versionCode = try { com.mandiri.newsapp.BuildConfig.VERSION_CODE.toString() } catch (_: Throwable) { "-" }
        Text(
            "Version $versionName | Build $versionCode",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    )
}

@Composable
private fun SpacerDivider() {
    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), thickness = 1.dp)
}

@Composable
private fun NavRow(
    title: String,
    trailingText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        if (!trailingText.isNullOrBlank()) {
            Text(
                trailingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SegmentedTwoOptions(
    left: String,
    right: String,
    selectedRight: Boolean,
    onSelectLeft: () -> Unit,
    onSelectRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val bg = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = modifier
            .height(32.dp)
            .clip(shape)
            .background(bg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .background(if (!selectedRight) MaterialTheme.colorScheme.surface else Color.Transparent)
                .clickable { onSelectLeft() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                left,
                style = MaterialTheme.typography.labelMedium,
                color = if (!selectedRight) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                .background(if (selectedRight) MaterialTheme.colorScheme.surface else Color.Transparent)
                .clickable { onSelectRight() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                right,
                style = MaterialTheme.typography.labelMedium,
                color = if (selectedRight) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
private fun formatRelativeOrEmpty(iso: String?): String =
    runCatching { iso?.take(10).orEmpty() }.getOrDefault("")
