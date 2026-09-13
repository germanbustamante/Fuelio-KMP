package com.germandebustamante.fuelio.core.domain.gasstation.usecase

import com.germandebustamante.fuelio.core.domain.gasstation.repository.PriceHistoryRepository
import com.germandebustamante.fuelio.core.domain.util.SPAIN_TIMEZONE
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** 90 days of history is enough for a trend chart to be meaningful without the table growing forever. */
private const val RETENTION_DAYS = 90

open class PrunePriceHistoryUseCase(private val priceHistoryRepository: PriceHistoryRepository, private val clock: Clock = Clock.System) {
    open suspend operator fun invoke() {
        val today = clock.todayIn(SPAIN_TIMEZONE)
        priceHistoryRepository.prune(cutoff = today.minus(RETENTION_DAYS, DateTimeUnit.DAY))
    }
}
