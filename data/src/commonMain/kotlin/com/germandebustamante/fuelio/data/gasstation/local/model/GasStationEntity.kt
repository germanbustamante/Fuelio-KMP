package com.germandebustamante.fuelio.data.gasstation.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GasStationEntity(
    @PrimaryKey val id: String,
    val provinceId: String,
    val name: String,
    val address: String,
    val city: String,
    val municipality: String,
    val province: String,
    val zipCode: String,
    val latitude: Double,
    val longitude: Double,
    val schedule: List<ScheduleSegmentEntity>,
    val gasolinePrice95: Double?,
    val gasolinePrice98: Double?,
    val dieselPrice: Double?,
    val dieselPremiumPrice: Double?,
)
