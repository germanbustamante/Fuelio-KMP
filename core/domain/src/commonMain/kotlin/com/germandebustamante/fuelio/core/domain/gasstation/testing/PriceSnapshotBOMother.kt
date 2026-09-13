package com.germandebustamante.fuelio.core.domain.gasstation.testing

import com.germandebustamante.fuelio.core.domain.gasstation.model.PriceSnapshotBO
import kotlinx.datetime.LocalDate

object PriceSnapshotBOMother {
    fun priceSnapshotBO(
        recordedOn: LocalDate = LocalDate(2026, 1, 1),
        gasolinePrice95: Double? = 1.65,
        gasolinePrice98: Double? = 1.78,
        dieselPrice: Double? = 1.55,
        dieselPremiumPrice: Double? = 1.68,
    ) = PriceSnapshotBO(
        recordedOn = recordedOn,
        gasolinePrice95 = gasolinePrice95,
        gasolinePrice98 = gasolinePrice98,
        dieselPrice = dieselPrice,
        dieselPremiumPrice = dieselPremiumPrice,
    )
}
