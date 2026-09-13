package com.germandebustamante.fuelio.data.gasstation.local.datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.germandebustamante.fuelio.data.gasstation.local.model.PriceSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceSnapshotDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: PriceSnapshotEntity)

    @Query("SELECT * FROM price_snapshots WHERE gasStationId = :gasStationId ORDER BY recordedOn DESC LIMIT 1")
    suspend fun getLatest(gasStationId: String): PriceSnapshotEntity?

    @Query("SELECT * FROM price_snapshots WHERE gasStationId = :gasStationId ORDER BY recordedOn ASC")
    fun observeHistory(gasStationId: String): Flow<List<PriceSnapshotEntity>>

    @Query("DELETE FROM price_snapshots WHERE recordedOn < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}
