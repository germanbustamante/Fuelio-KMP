package com.germandebustamante.fuelio.data.gasstation.model

import com.germandebustamante.fuelio.data.gasstation.remote.model.GasStationDTO

object GasStationDTOMother {

    fun gasStationDTO(
        id: String = "1",
        name: String = "REPSOL",
        address: String = "CALLE MAYOR 1",
        city: String = "MADRID",
        municipality: String = "MADRID",
        province: String = "MADRID",
        zipCode: String = "28001",
        latitude: String = "40,4168",
        longitude: String = "-3,7038",
        schedule: String = "L-D: 24H",
        gasolinePrice95: String? = "1,759",
        gasolinePrice98: String? = "1,889",
        dieselPrice: String? = "1,659",
        dieselPremiumPrice: String? = "1,729",
    ) = GasStationDTO(
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
        dieselPremiumPrice = dieselPremiumPrice,
    )
}
