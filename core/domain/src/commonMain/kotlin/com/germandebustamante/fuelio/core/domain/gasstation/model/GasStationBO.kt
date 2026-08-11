package com.germandebustamante.fuelio.core.domain.gasstation.model

import kotlinx.datetime.LocalDateTime

data class GasStationBO(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val municipality: String,
    val province: String,
    val zipCode: String,
    val latitude: Double,
    val longitude: Double,
    val schedule: List<ScheduleSegmentBO>,
    val gasolinePrice95: Double?,
    val gasolinePrice98: Double?,
    val dieselPrice: Double?,
    val dieselPremiumPrice: Double?,
) {
     val brand: GasStationBrand? by lazy(LazyThreadSafetyMode.NONE) { GasStationBrand.fromName(name) }

    val displayName: String by lazy(LazyThreadSafetyMode.NONE) { name.lowercase().replaceFirstChar { it.uppercase() } }

    fun getFullDirection() = "$address, $zipCode $municipality"

    fun isOpen(now: LocalDateTime): Boolean {
        val today = now.dayOfWeek
        val currentTime = now.time
        return schedule.any { segment ->
            if (!segment.coversDay(today)) return@any false
            val start = segment.startTime ?: return@any true
            val end = segment.endTime ?: return@any true
            if (start <= end) currentTime in start..end else currentTime >= start || currentTime <= end
        }
    }
}