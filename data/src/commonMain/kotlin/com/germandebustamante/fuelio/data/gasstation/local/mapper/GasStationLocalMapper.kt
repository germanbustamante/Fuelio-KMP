package com.germandebustamante.fuelio.data.gasstation.local.mapper

import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import com.germandebustamante.fuelio.core.domain.gasstation.model.ScheduleSegmentBO
import com.germandebustamante.fuelio.data.gasstation.local.model.GasStationEntity
import com.germandebustamante.fuelio.data.gasstation.local.model.ScheduleSegmentEntity

fun GasStationBO.toEntity(provinceId: String): GasStationEntity = GasStationEntity(
    id = id,
    provinceId = provinceId,
    name = name,
    address = address,
    city = city,
    municipality = municipality,
    province = province,
    zipCode = zipCode,
    latitude = latitude,
    longitude = longitude,
    schedule = schedule.map { it.toEntity() },
    gasolinePrice95 = gasolinePrice95,
    gasolinePrice98 = gasolinePrice98,
    dieselPrice = dieselPrice,
    dieselPremiumPrice = dieselPremiumPrice,
)

private fun ScheduleSegmentBO.toEntity() = ScheduleSegmentEntity(
    startDay = startDay,
    endDay = endDay,
    startTime = startTime,
    endTime = endTime,
)

fun GasStationEntity.toDomain(): GasStationBO = GasStationBO(
    id = id,
    name = name,
    address = address,
    city = city,
    municipality = municipality,
    province = province,
    zipCode = zipCode,
    latitude = latitude,
    longitude = longitude,
    schedule = schedule.map { it.toDomain() },
    gasolinePrice95 = gasolinePrice95,
    gasolinePrice98 = gasolinePrice98,
    dieselPrice = dieselPrice,
    dieselPremiumPrice = dieselPremiumPrice,
)

private fun ScheduleSegmentEntity.toDomain() = ScheduleSegmentBO(
    startDay = startDay,
    endDay = endDay,
    startTime = startTime,
    endTime = endTime,
)
