package com.germandebustamante.fuelio.data.gasstation

import com.germandebustamante.fuelio.core.domain.gasstation.testing.GasStationBOMother
import com.germandebustamante.fuelio.data.gasstation.local.datasource.PriceHistoryLocalDataSource
import com.germandebustamante.fuelio.data.gasstation.local.model.PriceSnapshotEntity
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Instant

class PriceHistoryRecorderTest {

    private val localDataSource: PriceHistoryLocalDataSource = mock {
        everySuspend { insert(any()) } returns Unit
    }

    // SPAIN_TIMEZONE, so today's epoch day is deterministic regardless of the machine running the test.
    private val fixedClock = FixedClock(TODAY.atStartOfDayIn(TimeZone.of("Europe/Madrid")))

    private val sut = PriceHistoryRecorder(localDataSource, fixedClock)

    @Test
    fun `record - GIVEN no prior snapshot WHEN called THEN a new snapshot is inserted for today`() = runTest {
        everySuspend { localDataSource.getLatest(any()) } returns null
        val station = GasStationBOMother.gasStationBO(id = STATION_ID, gasolinePrice95 = 1.65)

        sut.record(listOf(station))

        verifySuspend {
            localDataSource.insert(
                PriceSnapshotEntity(
                    gasStationId = STATION_ID,
                    recordedOn = TODAY.toEpochDays(),
                    gasolinePrice95 = station.gasolinePrice95,
                    gasolinePrice98 = station.gasolinePrice98,
                    dieselPrice = station.dieselPrice,
                    dieselPremiumPrice = station.dieselPremiumPrice,
                ),
            )
        }
    }

    @Test
    fun `record - GIVEN the latest snapshot has identical prices WHEN called THEN nothing is inserted`() = runTest {
        val station = GasStationBOMother.gasStationBO(id = STATION_ID, gasolinePrice95 = 1.65, gasolinePrice98 = 1.78, dieselPrice = 1.55, dieselPremiumPrice = 1.68)
        everySuspend { localDataSource.getLatest(STATION_ID) } returns PriceSnapshotEntity(
            gasStationId = STATION_ID,
            recordedOn = TODAY.toEpochDays() - 5,
            gasolinePrice95 = 1.65,
            gasolinePrice98 = 1.78,
            dieselPrice = 1.55,
            dieselPremiumPrice = 1.68,
        )

        sut.record(listOf(station))

        verifySuspend(dev.mokkery.verify.VerifyMode.not) { localDataSource.insert(any()) }
    }

    @Test
    fun `record - GIVEN the latest snapshot has a different price WHEN called THEN a new snapshot is inserted`() = runTest {
        val station = GasStationBOMother.gasStationBO(id = STATION_ID, gasolinePrice95 = 1.70)
        everySuspend { localDataSource.getLatest(STATION_ID) } returns PriceSnapshotEntity(
            gasStationId = STATION_ID,
            recordedOn = TODAY.toEpochDays() - 1,
            gasolinePrice95 = 1.65,
            gasolinePrice98 = station.gasolinePrice98,
            dieselPrice = station.dieselPrice,
            dieselPremiumPrice = station.dieselPremiumPrice,
        )

        sut.record(listOf(station))

        verifySuspend { localDataSource.insert(any()) }
    }

    private class FixedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private companion object {
        const val STATION_ID = "station-1"
        val TODAY = LocalDate(2026, 3, 15)
    }
}
