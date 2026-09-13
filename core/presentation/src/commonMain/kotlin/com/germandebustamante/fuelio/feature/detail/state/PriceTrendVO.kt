package com.germandebustamante.fuelio.feature.detail.state

import com.germandebustamante.fuelio.core.domain.gasstation.model.PriceHistoryBO
import kotlinx.datetime.LocalDate

data class PriceTrendPointVO(val recordedOn: LocalDate, val price: Double)

/**
 * Chart-ready trend data: [minPrice]/[maxPrice] are computed once here (never in Swift/Compose, per
 * the "business logic stays in Kotlin" invariant) so both platforms just scale [points] into their
 * canvas without recomputing anything.
 */
data class PriceTrendVO(val points: List<PriceTrendPointVO>, val minPrice: Double, val maxPrice: Double) {

    /** Where a price falls between [minPrice] and [maxPrice], as `0f` (bottom) to `1f` (top). A flat trend (no price variation) centers instead of dividing by zero. */
    fun normalizedY(price: Double): Float = if (maxPrice == minPrice) 0.5f else ((price - minPrice) / (maxPrice - minPrice)).toFloat()
}

/** `null` when there are fewer than two snapshots — a single point has no trend to draw. */
fun PriceHistoryBO.toPriceTrendVO(): PriceTrendVO? {
    val points = snapshots.mapNotNull { snapshot -> snapshot.cheapestPrice?.let { PriceTrendPointVO(snapshot.recordedOn, it) } }
    if (points.size < 2) return null
    val prices = points.map { it.price }
    return PriceTrendVO(points = points, minPrice = prices.min(), maxPrice = prices.max())
}
