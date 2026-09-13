package com.germandebustamante.fuelio.data.gasstation.local.model

import androidx.room.Entity

/**
 * One day's prices for a station. No foreign key to [GasStationEntity] on purpose: a station's row
 * is deleted and re-inserted on every province refresh
 * (`GasStationDAO.replaceGasStationsByProvince`), and a cascading FK would wipe the very history
 * this table exists to keep — same reasoning as `favorite_stations`.
 *
 * [gasStationId]+[recordedOn] (an epoch day in `SPAIN_TIMEZONE`) is the primary key: at most one row
 * per station per day, and re-inserting the same day with `REPLACE` is the dedupe mechanism.
 */
@Entity(tableName = "price_snapshots", primaryKeys = ["gasStationId", "recordedOn"])
data class PriceSnapshotEntity(
    val gasStationId: String,
    val recordedOn: Long,
    val gasolinePrice95: Double?,
    val gasolinePrice98: Double?,
    val dieselPrice: Double?,
    val dieselPremiumPrice: Double?,
)
