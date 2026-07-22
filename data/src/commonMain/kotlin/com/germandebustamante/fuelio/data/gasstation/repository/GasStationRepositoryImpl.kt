package com.germandebustamante.fuelio.data.gasstation.repository

import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationsResult
import com.germandebustamante.fuelio.core.domain.gasstation.repository.GasStationRepository
import com.germandebustamante.fuelio.data.gasstation.local.datasource.GasStationLocalDataSource
import com.germandebustamante.fuelio.data.gasstation.local.mapper.toDomain
import com.germandebustamante.fuelio.data.gasstation.local.mapper.toEntity
import com.germandebustamante.fuelio.data.gasstation.remote.datasource.GasStationRemoteDataSource
import com.germandebustamante.fuelio.data.gasstation.remote.model.toDomain
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GasStationRepositoryImpl(
    private val remoteDataSource: GasStationRemoteDataSource,
    private val localDataSource: GasStationLocalDataSource,
) : GasStationRepository {
    override fun getGasStationsByLocation(provinceId: String): Flow<Result<GasStationsResult>> = flow {
        val cachedStations = localDataSource.getGasStationsByProvince(provinceId).map { it.toDomain() }
        if (cachedStations.isNotEmpty()) {
            emit(Result.success(GasStationsResult(cachedStations, isFromCache = true)))
        }

        try {
            val remoteStations = remoteDataSource.getGasStationsByLocation(provinceId).map { it.toDomain() }
            localDataSource.insertGasStations(remoteStations.map { it.toEntity(provinceId) })
            emit(Result.success(GasStationsResult(remoteStations, isFromCache = false)))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getGasStationById(id: String): Flow<GasStationBO?> =
        localDataSource.getGasStationById(id)
            .map { entity -> entity?.toDomain() }
            .flowOn(Dispatchers.IO)
}
