package com.germandebustamante.fuelio.data.gasstation.repository

import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import com.germandebustamante.fuelio.core.domain.gasstation.repository.GasStationRepository
import com.germandebustamante.fuelio.data.gasstation.remote.datasource.GasStationRemoteDataSource
import com.germandebustamante.fuelio.data.gasstation.remote.model.toDomain
import com.germandebustamante.fuelio.data.util.safeApiCall
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GasStationRepositoryImpl(
    private val remoteDataSource: GasStationRemoteDataSource,
) : GasStationRepository {
    override fun getGasStationsByLocation(): Flow<Result<List<GasStationBO>>> = flow {
        try {
            emit(Result.success(safeApiCall { remoteDataSource.getGasStationsByLocation().map { it.toDomain() } }))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            emit(Result.failure(e))
        }
    }
}
