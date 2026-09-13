package com.germandebustamante.fuelio.core.startup

import com.germandebustamante.fuelio.core.domain.gasstation.usecase.PrunePriceHistoryUseCase

/**
 * Deletes price snapshots older than the retention window on every launch — no screen owns this, and
 * running it here (rather than on the write path in `PriceHistoryRecorder`) keeps a network refresh
 * from also paying for a delete query every time.
 */
class PriceHistoryPruneStartupTask(private val prunePriceHistoryUseCase: PrunePriceHistoryUseCase) : StartupTask {

    override suspend fun invoke() {
        prunePriceHistoryUseCase()
    }
}
