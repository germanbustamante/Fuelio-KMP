package com.germandebustamante.fuelio.data.gasstation.local.mapper

import com.germandebustamante.fuelio.core.domain.gasstation.model.PriceSnapshotBO
import com.germandebustamante.fuelio.data.gasstation.local.model.PriceSnapshotEntity
import kotlinx.datetime.LocalDate

fun PriceSnapshotEntity.toDomain() = PriceSnapshotBO(
    recordedOn = LocalDate.fromEpochDays(recordedOn),
    gasolinePrice95 = gasolinePrice95,
    gasolinePrice98 = gasolinePrice98,
    dieselPrice = dieselPrice,
    dieselPremiumPrice = dieselPremiumPrice,
)

fun PriceSnapshotBO.toEntity(gasStationId: String) = PriceSnapshotEntity(
    gasStationId = gasStationId,
    recordedOn = recordedOn.toEpochDays(),
    gasolinePrice95 = gasolinePrice95,
    gasolinePrice98 = gasolinePrice98,
    dieselPrice = dieselPrice,
    dieselPremiumPrice = dieselPremiumPrice,
)
