package com.germandebustamante.fuelio.core.domain.gasstation.usecase

import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import com.germandebustamante.fuelio.core.domain.gasstation.repository.GasStationRepository
import kotlinx.coroutines.flow.Flow

class GetGasStationsByLocationUseCase(private val gasStationRepository: GasStationRepository) {
    operator fun invoke(provinceId: String): Flow<Result<List<GasStationBO>>> =
        gasStationRepository.getGasStationsByLocation(provinceId)
}