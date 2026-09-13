package com.germandebustamante.fuelio.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Adds the price history table. Purely additive, same shape as [MIGRATION_1_2] — no existing column
 * or row is touched, so cached stations and favourites survive the upgrade.
 *
 * Hand-written rather than an `@AutoMigration`: the DDL is copied verbatim from the generated
 * `data/schemas/…/3.json`, which makes it reviewable in a diff and testable without a KSP round trip.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `price_snapshots` " +
                "(`gasStationId` TEXT NOT NULL, `recordedOn` INTEGER NOT NULL, `gasolinePrice95` REAL, " +
                "`gasolinePrice98` REAL, `dieselPrice` REAL, `dieselPremiumPrice` REAL, " +
                "PRIMARY KEY(`gasStationId`, `recordedOn`))",
        )
    }
}
