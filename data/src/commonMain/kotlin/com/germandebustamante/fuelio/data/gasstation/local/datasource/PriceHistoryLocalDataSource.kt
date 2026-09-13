package com.germandebustamante.fuelio.data.gasstation.local.datasource

import com.germandebustamante.fuelio.data.gasstation.local.model.PriceSnapshotEntity
import kotlinx.coroutines.flow.Flow

interface PriceHistoryLocalDataSource {
    suspend fun getLatest(gasStationId: String): PriceSnapshotEntity?
    suspend fun insert(snapshot: PriceSnapshotEntity)
    fun observeHistory(gasStationId: String): Flow<List<PriceSnapshotEntity>>
    suspend fun deleteOlderThan(cutoffEpochDay: Long)
}
