package com.germandebustamante.fuelio.data.gasstation.repository

import com.germandebustamante.fuelio.core.domain.gasstation.model.PriceHistoryBO
import com.germandebustamante.fuelio.core.domain.gasstation.repository.PriceHistoryRepository
import com.germandebustamante.fuelio.data.gasstation.local.datasource.PriceHistoryLocalDataSource
import com.germandebustamante.fuelio.data.gasstation.local.mapper.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class PriceHistoryRepositoryImpl(private val localDataSource: PriceHistoryLocalDataSource) : PriceHistoryRepository {
    override fun observeHistory(gasStationId: String): Flow<PriceHistoryBO> = localDataSource.observeHistory(gasStationId)
        .map { snapshots -> PriceHistoryBO(gasStationId, snapshots.map { it.toDomain() }) }
        .flowOn(Dispatchers.IO)

    override suspend fun prune(cutoff: LocalDate) = localDataSource.deleteOlderThan(cutoff.toEpochDays())
}
