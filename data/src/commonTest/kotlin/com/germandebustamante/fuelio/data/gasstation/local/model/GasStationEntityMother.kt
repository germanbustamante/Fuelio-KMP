package com.germandebustamante.fuelio.data.gasstation.local.model

import kotlinx.datetime.DayOfWeek

object GasStationEntityMother {

    fun gasStationEntity(
        id: String = "1",
        provinceId: String = "1",
        name: String = "REPSOL",
        address: String = "CALLE MAYOR 1",
        city: String = "MADRID",
        municipality: String = "MADRID",
        province: String = "MADRID",
        zipCode: String = "28001",
        latitude: Double = 40.4168,
        longitude: Double = -3.7038,
        schedule: List<ScheduleSegmentEntity> = alwaysOpenSchedule(),
        gasolinePrice95: Double? = 1.759,
        gasolinePrice98: Double? = 1.889,
        dieselPrice: Double? = 1.659,
        dieselPremiumPrice: Double? = 1.729,
    ) = GasStationEntity(
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
        schedule = schedule,
        gasolinePrice95 = gasolinePrice95,
        gasolinePrice98 = gasolinePrice98,
        dieselPrice = dieselPrice,
        dieselPremiumPrice = dieselPremiumPrice,
    )

    fun alwaysOpenSchedule() = listOf(
        ScheduleSegmentEntity(DayOfWeek.MONDAY, DayOfWeek.SUNDAY, null, null),
    )
}
