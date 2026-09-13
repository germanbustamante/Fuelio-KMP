package com.germandebustamante.fuelio.core.domain.gasstation.usecase

import com.germandebustamante.fuelio.core.domain.gasstation.model.PriceHistoryBO
import com.germandebustamante.fuelio.core.domain.gasstation.repository.PriceHistoryRepository
import kotlinx.coroutines.flow.Flow

open class ObservePriceHistoryUseCase(private val priceHistoryRepository: PriceHistoryRepository) {
    open operator fun invoke(gasStationId: String): Flow<PriceHistoryBO> = priceHistoryRepository.observeHistory(gasStationId)
}
