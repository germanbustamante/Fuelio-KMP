package com.germandebustamante.fuelio.data.gasstation.local.datasource

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.germandebustamante.fuelio.data.gasstation.local.model.FavoriteStationEntity
import com.germandebustamante.fuelio.data.gasstation.local.model.GasStationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStationDAO {

    @Query("SELECT gasStationId FROM favorite_stations")
    fun observeFavoriteIds(): Flow<List<String>>

    /**
     * The `INNER JOIN` is what makes a favourite resolvable: a station's details only exist while its
     * province is cached, and `replaceGasStationsByProvince` deletes rows the API stopped returning.
     * Favourites that can't be resolved simply don't appear — [observeFavoriteCount] is how the
     * screen can still tell the user some are missing.
     */
    @Query(
        """
        SELECT gs.* FROM GasStationEntity gs
        INNER JOIN favorite_stations f ON gs.id = f.gasStationId
        ORDER BY f.addedAt DESC
        """,
    )
    fun observeFavoriteStations(): Flow<List<GasStationEntity>>

    @Query("SELECT COUNT(*) FROM favorite_stations")
    fun observeFavoriteCount(): Flow<Int>

    @Upsert
    suspend fun addFavorite(favorite: FavoriteStationEntity)

    @Query("DELETE FROM favorite_stations WHERE gasStationId = :gasStationId")
    suspend fun removeFavorite(gasStationId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE gasStationId = :gasStationId)")
    suspend fun isFavorite(gasStationId: String): Boolean

    /** One transaction so a double tap can't leave the row half-written. */
    @Transaction
    suspend fun toggleFavorite(gasStationId: String, addedAt: Long) {
        if (isFavorite(gasStationId)) {
            removeFavorite(gasStationId)
        } else {
            addFavorite(FavoriteStationEntity(gasStationId, addedAt))
        }
    }
}
