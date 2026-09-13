package com.germandebustamante.fuelio.data.local.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pins the migration's DDL against `data/schemas/…/3.json`. Same reasoning as `Migration1To2Test`:
 * Room only verifies the schema at runtime on a device with a real prior-version database, so a
 * mismatch here would otherwise only surface as a crash on someone's upgrade.
 */
class Migration2To3Test {

    @Test
    fun `migrate - GIVEN a v2 database THEN it creates the price history table exactly as the schema declares`() {
        val connection = RecordingSQLiteConnection()

        MIGRATION_2_3.migrate(connection)

        assertEquals(listOf(EXPECTED_CREATE_SQL), connection.executed)
    }

    @Test
    fun `migrate - GIVEN the statement THEN it is additive and idempotent`() {
        val connection = RecordingSQLiteConnection()

        MIGRATION_2_3.migrate(connection)

        val sql = connection.executed.single()
        assertTrue(sql.startsWith("CREATE TABLE IF NOT EXISTS"), "re-running the migration must not fail")
        assertTrue(!sql.contains("DROP", ignoreCase = true), "the migration must not touch existing data")
    }

    @Test
    fun `version - GIVEN the migration THEN it goes from 2 to 3`() {
        assertEquals(2, MIGRATION_2_3.startVersion)
        assertEquals(3, MIGRATION_2_3.endVersion)
    }

    private class RecordingSQLiteConnection : SQLiteConnection {
        val executed = mutableListOf<String>()

        override fun prepare(sql: String): SQLiteStatement {
            executed += sql
            return NoOpSQLiteStatement
        }

        override fun close() = Unit
    }

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
        // Copied verbatim from data/schemas/…FuelioDatabase/3.json, with ${TABLE_NAME} resolved.
        const val EXPECTED_CREATE_SQL =
            "CREATE TABLE IF NOT EXISTS `price_snapshots` " +
                "(`gasStationId` TEXT NOT NULL, `recordedOn` INTEGER NOT NULL, `gasolinePrice95` REAL, " +
                "`gasolinePrice98` REAL, `dieselPrice` REAL, `dieselPremiumPrice` REAL, " +
                "PRIMARY KEY(`gasStationId`, `recordedOn`))"
    }
}
