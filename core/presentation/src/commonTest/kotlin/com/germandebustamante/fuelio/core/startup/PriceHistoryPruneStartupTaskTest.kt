package com.germandebustamante.fuelio.core.startup

import com.germandebustamante.fuelio.core.domain.gasstation.usecase.PrunePriceHistoryUseCase
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class PriceHistoryPruneStartupTaskTest {

    private val prunePriceHistoryUseCase: PrunePriceHistoryUseCase = mock {
        everySuspend { invoke() } returns Unit
    }

    @Test
    fun `invoke - WHEN run THEN the prune use case is called`() = runTest {
        PriceHistoryPruneStartupTask(prunePriceHistoryUseCase)()

        verifySuspend { prunePriceHistoryUseCase() }
    }
}
