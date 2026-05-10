package com.germandebustamante.fuelio.feature.common.permission.location

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionResultBridgeTest {

    //region awaitResult

    @Test
    fun `awaitResult - WHEN deliverResult called with true THEN returns true`() = runTest {
        var result: Boolean? = null

        launch { result = PermissionResultBridge.awaitResult() }
        advanceUntilIdle()
        PermissionResultBridge.deliverResult(true)
        advanceUntilIdle()

        assertTrue(result!!)
    }

    @Test
    fun `awaitResult - WHEN deliverResult called with false THEN returns false`() = runTest {
        var result: Boolean? = null

        launch { result = PermissionResultBridge.awaitResult() }
        advanceUntilIdle()
        PermissionResultBridge.deliverResult(false)
        advanceUntilIdle()

        assertFalse(result!!)
    }

    @Test
    fun `awaitResult - WHEN coroutine cancelled before deliverResult THEN continuation is cleared`() = runTest {
        var result: Boolean? = null

        val job = launch { result = PermissionResultBridge.awaitResult() }
        advanceUntilIdle()
        job.cancel()
        advanceUntilIdle()

        assertNull(result)
    }

    //endregion
}
