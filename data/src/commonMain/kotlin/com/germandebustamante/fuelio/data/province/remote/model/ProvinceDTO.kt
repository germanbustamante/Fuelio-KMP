package com.germandebustamante.fuelio.data.province.remote.model

import com.germandebustamante.fuelio.core.domain.province.model.ProvinceBO
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProvinceDTO(@SerialName("IDPovincia") val id: String, @SerialName("Provincia") val name: String)

fun ProvinceDTO.toDomain() = ProvinceBO(id = id, name = name)
