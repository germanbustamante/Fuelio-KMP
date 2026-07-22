package com.germandebustamante.fuelio.core.domain.gasstation.usecase

import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationsResult
import com.germandebustamante.fuelio.core.domain.gasstation.repository.GasStationRepository
import kotlinx.coroutines.flow.Flow

open class GetGasStationsByLocationUseCase(private val gasStationRepository: GasStationRepository) {
    open operator fun invoke(provinceId: String): Flow<Result<GasStationsResult>> =
        gasStationRepository.getGasStationsByLocation(provinceId)
}
