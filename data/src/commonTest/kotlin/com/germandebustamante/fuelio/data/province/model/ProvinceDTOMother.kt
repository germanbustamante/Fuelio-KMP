package com.germandebustamante.fuelio.data.province.model

import com.germandebustamante.fuelio.data.province.remote.model.ProvinceDTO

object ProvinceDTOMother {
    fun provinceDTO() = ProvinceDTO(
        id = "1",
        name = "Madrid",
    )

    fun provinceDTOList() = listOf(
        provinceDTO(),
        ProvinceDTO(id = "2", name = "Barcelona"),
    )
}
