package com.germandebustamante.fuelio.feature.detail.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.germandebustamante.fuelio.R
import com.germandebustamante.fuelio.core.ui.theme.FuelioSpacing
import com.germandebustamante.fuelio.core.ui.theme.FuelioTheme
import com.germandebustamante.fuelio.core.util.formatAsEuros
import com.germandebustamante.fuelio.designsystem.card.FuelioCard
import com.germandebustamante.fuelio.feature.detail.state.PriceTrendPointVO
import com.germandebustamante.fuelio.feature.detail.state.PriceTrendVO
import kotlinx.datetime.LocalDate

/**
 * A minimal line chart: [PriceTrendVO] already carries normalized-ready min/max, so this only maps
 * points onto a `Canvas` — no price math happens here, per the "business logic stays in Kotlin
 * (`:core:presentation`)" invariant.
 */
@Composable
fun PriceTrendChart(trend: PriceTrendVO, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(FuelioSpacing.sm)) {
        Text(
            text = stringResource(R.string.detail_price_trend_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FuelioCard {
            Column(modifier = Modifier.padding(FuelioSpacing.md)) {
                val lineColor = MaterialTheme.colorScheme.primary
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                ) {
                    val points = trend.points
                    if (points.size < 2) return@Canvas
                    val stepX = size.width / (points.size - 1)
                    val path = Path()
                    points.forEachIndexed { index, point ->
                        val x = index * stepX
                        val y = size.height * (1f - trend.normalizedY(point.price))
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path = path, color = lineColor, style = Stroke(width = 4f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = trend.minPrice.formatAsEuros(), style = MaterialTheme.typography.bodySmall)
                    Text(text = trend.maxPrice.formatAsEuros(), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun PriceTrendChartPreview() {
    FuelioTheme {
        PriceTrendChart(
            trend = PriceTrendVO(
                points = listOf(
                    PriceTrendPointVO(LocalDate(2026, 1, 1), 1.65),
                    PriceTrendPointVO(LocalDate(2026, 1, 5), 1.70),
                    PriceTrendPointVO(LocalDate(2026, 1, 10), 1.60),
                    PriceTrendPointVO(LocalDate(2026, 1, 15), 1.72),
                ),
                minPrice = 1.60,
                maxPrice = 1.72,
            ),
        )
    }
}
