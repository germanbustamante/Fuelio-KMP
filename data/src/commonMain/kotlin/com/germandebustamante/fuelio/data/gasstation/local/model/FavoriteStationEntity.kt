package com.germandebustamante.fuelio.data.gasstation.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A favourited station, in its own table rather than as a column on [GasStationEntity].
 *
 * `GasStationDAO.replaceGasStationsByProvince` is a `@Transaction { DELETE WHERE provinceId;
 * INSERT }` that runs on every network refresh, so a boolean column would be wiped each time the
 * list reloaded. A separate table survives that, spans provinces, and makes the 1→2 migration purely
 * additive.
 *
 * [addedAt] is epoch milliseconds and exists only to give the favourites screen a stable order.
 */
@Entity(tableName = "favorite_stations")
data class FavoriteStationEntity(@PrimaryKey val gasStationId: String, val addedAt: Long)
