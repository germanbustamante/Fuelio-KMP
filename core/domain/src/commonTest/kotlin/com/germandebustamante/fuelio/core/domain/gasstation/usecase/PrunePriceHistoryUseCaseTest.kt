package com.germandebustamante.fuelio.core.domain.gasstation.usecase

import com.germandebustamante.fuelio.core.domain.gasstation.repository.PriceHistoryRepository
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class PrunePriceHistoryUseCaseTest {

    @Test
    fun `invoke - GIVEN today's date in Spain THEN it prunes everything older than 90 days before it`() = runBlocking {
        val today = LocalDate(2026, 6, 15)
        val clock = FixedClock(today.atStartOfDayIn(TimeZone.of("Europe/Madrid")))
        val repository = RecordingPriceHistoryRepository()
        val sut = PrunePriceHistoryUseCase(repository, clock)

        sut()

        assertEquals(LocalDate(2026, 3, 17), repository.lastPrunedCutoff)
    }

    private class RecordingPriceHistoryRepository : PriceHistoryRepository {
        var lastPrunedCutoff: LocalDate? = null
            private set

        override fun observeHistory(gasStationId: String) = error("not used by this test")

        override suspend fun prune(cutoff: LocalDate) {
            lastPrunedCutoff = cutoff
        }
    }

    private class FixedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }
}
