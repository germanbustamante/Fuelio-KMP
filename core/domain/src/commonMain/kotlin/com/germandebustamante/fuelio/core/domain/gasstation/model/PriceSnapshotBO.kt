package com.germandebustamante.fuelio.core.domain.gasstation.model

import kotlinx.datetime.LocalDate

/** One day's prices for a station, recorded the first time that day's data was fetched. */
data class PriceSnapshotBO(
    val recordedOn: LocalDate,
    val gasolinePrice95: Double?,
    val gasolinePrice98: Double?,
    val dieselPrice: Double?,
    val dieselPremiumPrice: Double?,
) {
    /** The trend chart's one series: whichever fuel was cheapest that day, mirroring the "cheapest of the four" highlight the list and detail screens already show. */
    val cheapestPrice: Double? by lazy(LazyThreadSafetyMode.NONE) {
        listOfNotNull(gasolinePrice95, gasolinePrice98, dieselPrice, dieselPremiumPrice).minOrNull()
    }
}
