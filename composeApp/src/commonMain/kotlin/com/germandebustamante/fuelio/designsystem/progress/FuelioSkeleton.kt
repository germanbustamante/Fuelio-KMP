package com.germandebustamante.fuelio.designsystem.progress

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.designsystem.card.FuelioCard

@Composable
fun Modifier.fuelioSkeleton(shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.small): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val offset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart,
        ),
        label = "skeleton_offset",
    )
    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val shimmerColor = MaterialTheme.colorScheme.surface
    val brush = Brush.linearGradient(
        colors = listOf(baseColor, shimmerColor, baseColor),
        start = Offset(offset * 600f, 0f),
        end = Offset((offset + 1f) * 600f, 0f),
    )
    return this
        .clip(shape)
        .background(brush)
}

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.small,
) {
    val boxModifier = if (width != null) {
        modifier.width(width).height(height)
    } else {
        modifier.fillMaxWidth().height(height)
    }
    Box(modifier = boxModifier.fuelioSkeleton(shape))
}

/**
 * Mirrors [com.germandebustamante.fuelio.feature.list.ui.GasStationItem]'s layout
 * (price+status row, avatar+name row, address+distance row) so the skeleton-to-content
 * transition doesn't jump.
 */
@Composable
fun FuelioGasStationItemSkeleton(modifier: Modifier = Modifier) {
    FuelioCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FuelioSpacing.md),
            verticalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SkeletonBox(height = 30.dp, width = 96.dp, shape = MaterialTheme.shapes.small)
                SkeletonBox(height = 24.dp, width = 88.dp, shape = MaterialTheme.shapes.small)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(FuelioSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .fuelioSkeleton(CircleShape),
                )
                SkeletonBox(height = 16.dp, width = 140.dp, shape = MaterialTheme.shapes.extraSmall)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SkeletonBox(height = 13.dp, width = 170.dp, shape = MaterialTheme.shapes.extraSmall)
                SkeletonBox(height = 13.dp, width = 40.dp, shape = MaterialTheme.shapes.extraSmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FuelioGasStationItemSkeletonPreview() {
    FuelioTheme {
        Column {
            repeat(3) { FuelioGasStationItemSkeleton() }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SkeletonBoxPreview() {
    FuelioTheme {
        Column(
            modifier = Modifier.padding(FuelioSpacing.md),
        ) {
            SkeletonBox(height = 16.dp, width = 200.dp)
            Spacer(Modifier.height(FuelioSpacing.sm))
            SkeletonBox(height = 12.dp, width = 140.dp)
            Spacer(Modifier.height(FuelioSpacing.sm))
            SkeletonBox(height = 80.dp)
        }
    }
}
