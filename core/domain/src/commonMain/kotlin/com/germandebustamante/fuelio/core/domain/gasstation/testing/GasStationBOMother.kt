package com.germandebustamante.fuelio.core.domain.gasstation.testing

import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import com.germandebustamante.fuelio.core.domain.gasstation.model.ScheduleSegmentBO
import kotlinx.datetime.DayOfWeek

object GasStationBOMother {
    fun gasStationBO(
        id: String = "1",
        name: String = "Gasoil Center",
        address: String = "Calle Principal 123",
        city: String = "Madrid",
        municipality: String = "Madrid",
        province: String = "Madrid",
        zipCode: String = "28001",
        latitude: Double = 40.4168,
        longitude: Double = -3.7038,
        schedule: List<ScheduleSegmentBO> = alwaysOpenSchedule(),
        gasolinePrice95: Double? = 1.65,
        gasolinePrice98: Double? = 1.78,
        dieselPrice: Double? = 1.55,
        dieselPremiumPrice: Double? = 1.68,
    ) = GasStationBO(
        id = id,
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
        dieselPremiumPrice = dieselPremiumPrice
    )

    fun gasStationBOList() = listOf(
        gasStationBO(),
        gasStationBO(id = "2", name = "Fuel Express")
    )

    fun alwaysOpenSchedule() = listOf(
        ScheduleSegmentBO(DayOfWeek.MONDAY, DayOfWeek.SUNDAY, null, null)
    )
}
