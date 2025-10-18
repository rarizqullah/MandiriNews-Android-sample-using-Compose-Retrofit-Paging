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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.luminance
import androidx.compose.material.icons.outlined.PlayCircleOutline




@Composable
fun GlassNavigationBar(
    current: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp
) {
    val items = listOf(
        Icons.Outlined.Home to "Home",
        Icons.Outlined.PlayCircleOutline to "Watch",
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
        Box(
            modifier = Modifier
                .matchParentSize()
                .zIndex(0f)
                .clip(shape)
                .blur(16.dp)
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

@Composable
private fun SelectedGlassPill(
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    val surfaceLum = MaterialTheme.colorScheme.surface.luminance()
    val isDark = surfaceLum < 0.5f

    val fillAlpha   = if (isDark) 0.14f else 0.18f
    val borderAlpha = if (isDark) 0.30f else 0.45f
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(accent.copy(alpha = fillAlpha))
            .border(1.dp, accent.copy(alpha = borderAlpha), shape)

    )
}

