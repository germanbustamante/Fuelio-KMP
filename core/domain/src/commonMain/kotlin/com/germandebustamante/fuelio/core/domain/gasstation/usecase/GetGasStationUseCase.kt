package com.germandebustamante.fuelio.core.domain.gasstation.usecase

import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import com.germandebustamante.fuelio.core.domain.gasstation.repository.GasStationRepository
import kotlinx.coroutines.flow.Flow

open class GetGasStationUseCase(private val gasStationRepository: GasStationRepository) {
    open operator fun invoke(id: String): Flow<GasStationBO?> = gasStationRepository.getGasStationById(id)
}
