package com.mandiri.newsapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * Bottom navigation transparan ala "liquid glass".
 * 3 tombol: Home, Saved, Settings.
 *
 * Catatan penting:
 * - Kaca (blur) digambar pada UNDERLAY (Box matchParentSize) sehingga ikon tidak ikut blur.
 */
@Composable
fun GlassNavigationBar(
    current: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp
) {
    val items = listOf(
        Icons.Outlined.Home to "Home",
        Icons.Outlined.BookmarkBorder to "Saved",
        Icons.Outlined.Settings to "Settings"
    )
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
    ) {
        // === UNDERLAY: kaca (blur + warna bening + border + glare) ===
        Box(
            modifier = Modifier
                .matchParentSize()
                .zIndex(0f)
                .clip(shape)
                .blur(16.dp) // blur hanya pada underlay ini
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                    shape = shape
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    shape = shape
                )
                .drawBehind {
                    // glare lembut di bagian atas
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            0f to Color.White.copy(alpha = 0.22f),
                            0.35f to Color.White.copy(alpha = 0.08f),
                            1f to Color.Transparent
                        ),
                        cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
                    )
                }
        )

        // === KONTEN: ikon di atas kaca (tidak kena blur) ===
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 10.dp)
                .zIndex(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, (icon, cd) ->
                val selected = current == index
                val interaction = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(vertical = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = interaction,
                            indication = null
                        ) { onChange(index) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        SelectedGlassPill(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 8.dp)
                                .aspectRatio(1.35f)
                                .align(Alignment.Center),
                            accent = MaterialTheme.colorScheme.primary
                        )
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = cd,
                        modifier = Modifier.size(22.dp),
                        tint = if (selected)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f) // sedikit lebih pekat
                    )
                }
            }
        }
    }
}

/** Badge kaca elips yang muncul di belakang ikon terpilih. */
@Composable
private fun SelectedGlassPill(
    modifier: Modifier = Modifier,
    accent: Color = Color(0xFFFFC400)
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(accent.copy(alpha = 0.18f))
            .border(1.dp, accent.copy(alpha = 0.45f), shape)
            .drawBehind {
                // sedikit highlight diagonal
                val w = size.width
                val h = size.height
                val band = w * 0.38f
                translate(left = w * 0.15f, top = 0f) {
                    drawRect(
                        brush = Brush.linearGradient(
                            listOf(
                                accent.copy(alpha = 0f),
                                Color.White.copy(alpha = 0.60f),
                                accent.copy(alpha = 0f)
                            )
                        ),
                        size = androidx.compose.ui.geometry.Size(band, h)
                    )
                }
            }
    )
}
