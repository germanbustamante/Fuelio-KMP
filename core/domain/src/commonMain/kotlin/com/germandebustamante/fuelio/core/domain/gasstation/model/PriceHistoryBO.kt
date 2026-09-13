package com.germandebustamante.fuelio.core.domain.gasstation.model

/** [snapshots] is ordered oldest first, ready for a trend chart to plot left-to-right. */
data class PriceHistoryBO(val gasStationId: String, val snapshots: List<PriceSnapshotBO>)
