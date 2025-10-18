package com.mandiri.newsapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mandiri.newsapp.data.remote.model.Article
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ArticleListItem(
    article: Article,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isBookmarked: Boolean = false,
    onToggleBookmark: (Article) -> Unit = {}
) {
    val link = article.url
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !link.isNullOrBlank()) { link?.let(onClick) }
            .padding(vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 84.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = article.urlToImage,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = { onToggleBookmark(article) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(18.dp)
                ) {
                    val accent = MaterialTheme.colorScheme.primary

                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        modifier = Modifier.size(18.dp),
                        tint = if (isBookmarked) accent else accent.copy(alpha = 0.75f)
                    )

                }

            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = article.title.orEmpty().ifBlank { "(Untitled)" },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!article.description.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = article.description!!,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = article.source?.name.orEmpty(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = formatDateSafe(article.publishedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
    }
}

private fun formatDateSafe(iso: String?): String = runCatching {
    if (iso.isNullOrBlank()) return ""
    val inst = Instant.parse(iso)
    val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
    fmt.format(inst.atZone(ZoneId.systemDefault()))
}.getOrDefault("")
