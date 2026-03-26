package com.germandebustamante.fuelio.core.domain.gasstation.model

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
    val schedule: String,
    val gasolinePrice95: Double?,
    val gasolinePrice98: Double?,
    val dieselPrice: Double?,
    val dieselPremiumPrice: Double?,
) {
    fun getFullDirection() = "$address, $zipCode $municipality"
}