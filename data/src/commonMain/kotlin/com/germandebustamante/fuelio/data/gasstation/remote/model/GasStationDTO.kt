package com.germandebustamante.fuelio.data.gasstation.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GasStationResponseDTO(@SerialName("ListaEESSPrecio") val stations: List<GasStationDTO>)

@Serializable
data class GasStationDTO(
    @SerialName("IDEESS") val id: String,
    @SerialName("Rótulo") val name: String,
    @SerialName("Dirección") val address: String,
    @SerialName("Localidad") val city: String,
    @SerialName("Municipio") val municipality: String,
    @SerialName("Provincia") val province: String,
    @SerialName("C.P.") val zipCode: String,
    @SerialName("Latitud") val latitude: String,
    @SerialName("Longitud (WGS84)") val longitude: String,
    @SerialName("Horario") val schedule: String,
    @SerialName("Precio Gasolina 95 E5") val gasolinePrice95: String?,
    @SerialName("Precio Gasolina 98 E5") val gasolinePrice98: String?,
    @SerialName("Precio Gasoleo A") val dieselPrice: String?,
    @SerialName("Precio Gasoleo Premium") val dieselPremiumPrice: String?,
)
