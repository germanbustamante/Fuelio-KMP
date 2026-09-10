package com.germandebustamante.fuelio.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Adds the favourites table. Purely additive — no existing column or row is touched, so cached
 * stations survive the upgrade.
 *
 * Hand-written rather than an `@AutoMigration`: the DDL is copied verbatim from the generated
 * `data/schemas/…/2.json`, which makes it reviewable in a diff and testable without a KSP round trip.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `favorite_stations` " +
                "(`gasStationId` TEXT NOT NULL, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`gasStationId`))",
        )
    }
}
