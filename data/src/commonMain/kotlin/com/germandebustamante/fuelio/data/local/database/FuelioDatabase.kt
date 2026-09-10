package com.germandebustamante.fuelio.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.germandebustamante.fuelio.data.di.ContextProvider
import com.germandebustamante.fuelio.data.gasstation.local.datasource.FavoriteStationDAO
import com.germandebustamante.fuelio.data.gasstation.local.datasource.GasStationDAO
import com.germandebustamante.fuelio.data.gasstation.local.model.FavoriteStationEntity
import com.germandebustamante.fuelio.data.gasstation.local.model.GasStationEntity
import com.germandebustamante.fuelio.data.local.database.migration.MIGRATION_1_2
import com.germandebustamante.fuelio.data.local.typeconverter.DayOfWeekConverter
import com.germandebustamante.fuelio.data.local.typeconverter.LocalTimeConverter
import com.germandebustamante.fuelio.data.local.typeconverter.ScheduleSegmentListConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [GasStationEntity::class, FavoriteStationEntity::class], version = 2)
@TypeConverters(DayOfWeekConverter::class, LocalTimeConverter::class, ScheduleSegmentListConverter::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class FuelioDatabase : RoomDatabase() {
    abstract fun gasStationDao(): GasStationDAO

    abstract fun favoriteStationDao(): FavoriteStationDAO
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<FuelioDatabase> {
    override fun initialize(): FuelioDatabase
}

fun getRoomDatabase(builder: RoomDatabase.Builder<FuelioDatabase>): FuelioDatabase = builder
    .addMigrations(MIGRATION_1_2)
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()

expect fun getDatabaseBuilder(contextProvider: ContextProvider): RoomDatabase.Builder<FuelioDatabase>
