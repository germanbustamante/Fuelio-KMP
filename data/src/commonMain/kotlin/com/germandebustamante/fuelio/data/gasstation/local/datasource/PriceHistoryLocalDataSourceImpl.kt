package com.germandebustamante.fuelio.data.gasstation.local.datasource

import com.germandebustamante.fuelio.data.gasstation.local.model.PriceSnapshotEntity
import kotlinx.coroutines.flow.Flow

class PriceHistoryLocalDataSourceImpl(private val priceSnapshotDAO: PriceSnapshotDAO) : PriceHistoryLocalDataSource {
    override suspend fun getLatest(gasStationId: String): PriceSnapshotEntity? = priceSnapshotDAO.getLatest(gasStationId)

    override suspend fun insert(snapshot: PriceSnapshotEntity) = priceSnapshotDAO.insert(snapshot)

    override fun observeHistory(gasStationId: String): Flow<List<PriceSnapshotEntity>> = priceSnapshotDAO.observeHistory(gasStationId)

    override suspend fun deleteOlderThan(cutoffEpochDay: Long) = priceSnapshotDAO.deleteOlderThan(cutoffEpochDay)
}
