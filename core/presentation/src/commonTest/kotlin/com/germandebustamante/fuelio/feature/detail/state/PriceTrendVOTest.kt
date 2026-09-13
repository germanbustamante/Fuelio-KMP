package com.germandebustamante.fuelio.feature.detail.state

import com.germandebustamante.fuelio.core.domain.gasstation.model.PriceHistoryBO
import com.germandebustamante.fuelio.core.domain.gasstation.testing.PriceSnapshotBOMother
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PriceTrendVOTest {

    @Test
    fun `toPriceTrendVO - GIVEN fewer than two snapshots THEN it is null`() {
        val history = PriceHistoryBO(STATION_ID, listOf(PriceSnapshotBOMother.priceSnapshotBO()))

        assertNull(history.toPriceTrendVO())
    }

    @Test
    fun `toPriceTrendVO - GIVEN no snapshots THEN it is null`() {
        val history = PriceHistoryBO(STATION_ID, emptyList())

        assertNull(history.toPriceTrendVO())
    }

    @Test
    fun `toPriceTrendVO - GIVEN two or more snapshots THEN each point is the cheapest price that day`() {
        val history = PriceHistoryBO(
            STATION_ID,
            listOf(
                PriceSnapshotBOMother.priceSnapshotBO(
                    recordedOn = LocalDate(2026, 1, 1),
                    gasolinePrice95 = 1.70,
                    gasolinePrice98 = 1.85,
                    dieselPrice = 1.60,
                    dieselPremiumPrice = 1.75,
                ),
                PriceSnapshotBOMother.priceSnapshotBO(
                    recordedOn = LocalDate(2026, 1, 2),
                    gasolinePrice95 = 1.72,
                    gasolinePrice98 = 1.86,
                    dieselPrice = 1.58,
                    dieselPremiumPrice = 1.74,
                ),
            ),
        )

        val trend = history.toPriceTrendVO()

        assertEquals(
            listOf(
                PriceTrendPointVO(LocalDate(2026, 1, 1), 1.60),
                PriceTrendPointVO(LocalDate(2026, 1, 2), 1.58),
            ),
            trend?.points,
        )
    }

    @Test
    fun `toPriceTrendVO - WHEN computed THEN minPrice and maxPrice span every point`() {
        val history = PriceHistoryBO(
            STATION_ID,
            listOf(
                PriceSnapshotBOMother.priceSnapshotBO(recordedOn = LocalDate(2026, 1, 1), gasolinePrice95 = 1.60, gasolinePrice98 = null, dieselPrice = null, dieselPremiumPrice = null),
                PriceSnapshotBOMother.priceSnapshotBO(recordedOn = LocalDate(2026, 1, 2), gasolinePrice95 = 1.70, gasolinePrice98 = null, dieselPrice = null, dieselPremiumPrice = null),
                PriceSnapshotBOMother.priceSnapshotBO(recordedOn = LocalDate(2026, 1, 3), gasolinePrice95 = 1.55, gasolinePrice98 = null, dieselPrice = null, dieselPremiumPrice = null),
            ),
        )

        val trend = requireNotNull(history.toPriceTrendVO())

        assertEquals(1.55, trend.minPrice)
        assertEquals(1.70, trend.maxPrice)
    }

    @Test
    fun `normalizedY - GIVEN a flat trend THEN it centers instead of dividing by zero`() {
        val trend = PriceTrendVO(points = emptyList(), minPrice = 1.60, maxPrice = 1.60)

        assertEquals(0.5f, trend.normalizedY(1.60))
    }

    @Test
    fun `normalizedY - GIVEN the minimum price THEN it is zero`() {
        val trend = PriceTrendVO(points = emptyList(), minPrice = 1.50, maxPrice = 1.70)

        assertEquals(0f, trend.normalizedY(1.50))
    }

    @Test
    fun `normalizedY - GIVEN the maximum price THEN it is one`() {
        val trend = PriceTrendVO(points = emptyList(), minPrice = 1.50, maxPrice = 1.70)

        assertEquals(1f, trend.normalizedY(1.70))
    }

    private companion object {
        const val STATION_ID = "station-1"
    }
}
