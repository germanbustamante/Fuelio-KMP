package com.germandebustamante.fuelio.data.gasstation.repository

import app.cash.turbine.test
import com.germandebustamante.fuelio.data.gasstation.local.datasource.PriceHistoryLocalDataSource
import com.germandebustamante.fuelio.data.gasstation.local.model.PriceSnapshotEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class PriceHistoryRepositoryImplTest {

    @Test
    fun `observeHistory - GIVEN snapshots stored oldest first WHEN observed THEN they map to domain in the same order`() = runTest {
        val older = PriceSnapshotEntity(STATION_ID, recordedOn = 19_000, gasolinePrice95 = 1.60, gasolinePrice98 = null, dieselPrice = null, dieselPremiumPrice = null)
        val newer = PriceSnapshotEntity(STATION_ID, recordedOn = 19_010, gasolinePrice95 = 1.65, gasolinePrice98 = null, dieselPrice = null, dieselPremiumPrice = null)
        val localDataSource = InMemoryPriceHistoryLocalDataSource(listOf(older, newer))

        PriceHistoryRepositoryImpl(localDataSource).observeHistory(STATION_ID).test {
            val history = awaitItem()
            assertEquals(STATION_ID, history.gasStationId)
            assertEquals(listOf(LocalDate.fromEpochDays(19_000), LocalDate.fromEpochDays(19_010)), history.snapshots.map { it.recordedOn })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `prune - WHEN called THEN the cutoff date is converted to an epoch day before deleting`() = runTest {
        val localDataSource = InMemoryPriceHistoryLocalDataSource(emptyList())

        PriceHistoryRepositoryImpl(localDataSource).prune(LocalDate.fromEpochDays(19_005))

        assertEquals(19_005, localDataSource.lastDeletedCutoff)
    }

    private class InMemoryPriceHistoryLocalDataSource(initialSnapshots: List<PriceSnapshotEntity>) : PriceHistoryLocalDataSource {
        private val snapshots = MutableStateFlow(initialSnapshots)
        var lastDeletedCutoff: Long? = null
            private set

        override suspend fun getLatest(gasStationId: String): PriceSnapshotEntity? = snapshots.value.filter { it.gasStationId == gasStationId }.maxByOrNull { it.recordedOn }

        override suspend fun insert(snapshot: PriceSnapshotEntity) {
            snapshots.value = snapshots.value + snapshot
        }

        override fun observeHistory(gasStationId: String): Flow<List<PriceSnapshotEntity>> = MutableStateFlow(snapshots.value.filter { it.gasStationId == gasStationId }.sortedBy { it.recordedOn })

        override suspend fun deleteOlderThan(cutoffEpochDay: Long) {
            lastDeletedCutoff = cutoffEpochDay
            snapshots.value = snapshots.value.filter { it.recordedOn >= cutoffEpochDay }
        }
    }

    private companion object {
        const val STATION_ID = "station-1"
    }
}
