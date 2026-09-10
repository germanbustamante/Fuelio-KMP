package com.germandebustamante.fuelio.data.local.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pins the migration's DDL against `data/schemas/…/2.json`.
 *
 * Room only verifies the schema at runtime, on a device that actually has a v1 database — which no
 * test in this project has. The failure mode is an `IllegalStateException` on first launch after an
 * update, for users who upgrade and nobody who installs fresh, so it is worth catching statically:
 * if the entity changes and this string doesn't, the assertion below fails.
 */
class Migration1To2Test {

    @Test
    fun `migrate - GIVEN a v1 database THEN it creates the favorites table exactly as the schema declares`() {
        val connection = RecordingSQLiteConnection()

        MIGRATION_1_2.migrate(connection)

        assertEquals(listOf(EXPECTED_CREATE_SQL), connection.executed)
    }

    @Test
    fun `migrate - GIVEN the statement THEN it is additive and idempotent`() {
        val connection = RecordingSQLiteConnection()

        MIGRATION_1_2.migrate(connection)

        val sql = connection.executed.single()
        assertTrue(sql.startsWith("CREATE TABLE IF NOT EXISTS"), "re-running the migration must not fail")
        assertTrue(!sql.contains("DROP", ignoreCase = true), "the migration must not touch existing data")
    }

    @Test
    fun `version - GIVEN the migration THEN it goes from 1 to 2`() {
        assertEquals(1, MIGRATION_1_2.startVersion)
        assertEquals(2, MIGRATION_1_2.endVersion)
    }

    private class RecordingSQLiteConnection : SQLiteConnection {
        val executed = mutableListOf<String>()

        override fun prepare(sql: String): SQLiteStatement {
            executed += sql
            return NoOpSQLiteStatement
        }

        override fun close() = Unit
    }

    /**
     * `execSQL` prepares, steps and closes; nothing here needs a real result, so every read throws
     * to make an accidental query obvious rather than silently returning a default.
     */
    private object NoOpSQLiteStatement : SQLiteStatement {
        override fun bindBlob(index: Int, value: ByteArray) = Unit
        override fun bindDouble(index: Int, value: Double) = Unit
        override fun bindLong(index: Int, value: Long) = Unit
        override fun bindText(index: Int, value: String) = Unit
        override fun bindNull(index: Int) = Unit
        override fun getBlob(index: Int): ByteArray = unsupported()
        override fun getDouble(index: Int): Double = unsupported()
        override fun getLong(index: Int): Long = unsupported()
        override fun getText(index: Int): String = unsupported()
        override fun isNull(index: Int): Boolean = unsupported()
        override fun getColumnCount(): Int = 0
        override fun getColumnType(index: Int): Int = unsupported()
        override fun getColumnName(index: Int): String = unsupported()
        override fun step(): Boolean = false
        override fun reset() = Unit
        override fun clearBindings() = Unit
        override fun close() = Unit

        private fun unsupported(): Nothing = error("the migration must not read rows")
    }

    private companion object {
        // Copied verbatim from data/schemas/…FuelioDatabase/2.json, with ${TABLE_NAME} resolved.
        const val EXPECTED_CREATE_SQL =
            "CREATE TABLE IF NOT EXISTS `favorite_stations` " +
                "(`gasStationId` TEXT NOT NULL, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`gasStationId`))"
    }
}
