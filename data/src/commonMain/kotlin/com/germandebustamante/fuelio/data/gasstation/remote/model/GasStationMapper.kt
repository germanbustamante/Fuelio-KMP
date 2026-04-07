package com.germandebustamante.fuelio.data.gasstation.remote.model

import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO

fun GasStationDTO.toDomain() = GasStationBO(
    id = id,
    name = name,
    address = address,
    city = city,
    municipality = municipality,
    province = province,
    zipCode = zipCode,
    latitude = latitude.parseToDoubleOrNull() ?: 0.0,
    longitude = longitude.parseToDoubleOrNull() ?: 0.0,
    schedule = schedule,
    gasolinePrice95 = gasolinePrice95?.parseToDoubleOrNull(),
    gasolinePrice98 = gasolinePrice98?.parseToDoubleOrNull(),
    dieselPrice = dieselPrice?.parseToDoubleOrNull(),
    dieselPremiumPrice = dieselPremiumPrice?.parseToDoubleOrNull(),
)

private fun String.parseToDoubleOrNull(): Double? = replace(",", ".").toDoubleOrNull()
