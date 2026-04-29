package com.germandebustamante.fuelio.data.gasstation.repository

import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import com.germandebustamante.fuelio.core.domain.gasstation.repository.GasStationRepository
import com.germandebustamante.fuelio.data.gasstation.remote.datasource.GasStationRemoteDataSource
import com.germandebustamante.fuelio.data.gasstation.remote.model.toDomain
import com.germandebustamante.fuelio.data.util.resultFlow
import kotlinx.coroutines.flow.Flow

class GasStationRepositoryImpl(
    private val remoteDataSource: GasStationRemoteDataSource,
) : GasStationRepository {
    override fun getGasStationsByLocation(provinceId: String): Flow<Result<List<GasStationBO>>> =
        resultFlow { remoteDataSource.getGasStationsByLocation(provinceId).map { it.toDomain() } }
}
