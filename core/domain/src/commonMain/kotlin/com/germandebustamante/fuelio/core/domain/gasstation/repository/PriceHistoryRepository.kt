package com.germandebustamante.fuelio.core.domain.gasstation.repository

import com.germandebustamante.fuelio.core.domain.gasstation.model.PriceHistoryBO
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Purely local, like [com.germandebustamante.fuelio.core.domain.gasstation.repository.GasStationRepository.getGasStationById]:
 * there is no remote history endpoint, only what this device has already recorded, so `null`/empty
 * is an expected value rather than a failure worth wrapping in `Result`.
 */
interface PriceHistoryRepository {
    fun observeHistory(gasStationId: String): Flow<PriceHistoryBO>

    /** Deletes every snapshot recorded before [cutoff]. Called from a startup task, never from the write path. */
    suspend fun prune(cutoff: LocalDate)
}
