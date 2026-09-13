package com.germandebustamante.fuelio.data.gasstation

import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import com.germandebustamante.fuelio.core.domain.util.SPAIN_TIMEZONE
import com.germandebustamante.fuelio.data.gasstation.local.datasource.PriceHistoryLocalDataSource
import com.germandebustamante.fuelio.data.gasstation.local.model.PriceSnapshotEntity
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * Writes one price snapshot per station per day, called by [com.germandebustamante.fuelio.data.gasstation.repository.GasStationRepositoryImpl]
 * right after a province refresh — a separate collaborator rather than inline in the repository, so
 * the stale-while-revalidate contract there stays about caching, and this can be mocked out in
 * `GasStationRepositoryImplTest` without a real Room instance.
 *
 * Skips the write entirely when the four prices match the last saved snapshot (whatever day it was
 * recorded), so a station whose prices never change never grows a row per day — the chart's flat
 * stretches are two points apart, not one per day.
 */
open class PriceHistoryRecorder(private val localDataSource: PriceHistoryLocalDataSource, private val clock: Clock = Clock.System) {

    open suspend fun record(gasStations: List<GasStationBO>) {
        val today = clock.todayIn(SPAIN_TIMEZONE).toEpochDays()
        gasStations.forEach { station ->
            val latest = localDataSource.getLatest(station.id)
            val unchanged = latest != null &&
                latest.gasolinePrice95 == station.gasolinePrice95 &&
                latest.gasolinePrice98 == station.gasolinePrice98 &&
                latest.dieselPrice == station.dieselPrice &&
                latest.dieselPremiumPrice == station.dieselPremiumPrice
            if (unchanged) return@forEach

            localDataSource.insert(
                PriceSnapshotEntity(
                    gasStationId = station.id,
                    recordedOn = today,
                    gasolinePrice95 = station.gasolinePrice95,
                    gasolinePrice98 = station.gasolinePrice98,
                    dieselPrice = station.dieselPrice,
                    dieselPremiumPrice = station.dieselPremiumPrice,
                ),
            )
        }
    }
}
