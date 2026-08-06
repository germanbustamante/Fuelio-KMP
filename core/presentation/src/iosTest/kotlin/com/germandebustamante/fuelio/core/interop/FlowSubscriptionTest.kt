package com.germandebustamante.fuelio.core.interop

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FlowSubscriptionTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `case - GIVEN a finite flow WHEN subscribed THEN every emission reaches the callback in order`() = runTest(testDispatcher) {
        val received = mutableListOf<String>()

        flowOf("a", "b", "c").subscribe(testDispatcher) { received += it }
        advanceUntilIdle()

        assertEquals(listOf("a", "b", "c"), received)
    }

    @Test
    fun `case - GIVEN a state flow WHEN subscribed THEN the current value is delivered immediately`() = runTest(testDispatcher) {
        val source = MutableStateFlow(1)
        val received = mutableListOf<Int>()

        source.subscribe(testDispatcher) { received += it }
        advanceUntilIdle()

        assertEquals(listOf(1), received)
    }

    @Test
    fun `case - GIVEN an active subscription WHEN cancelled THEN later emissions are not delivered`() = runTest(testDispatcher) {
        val source = MutableStateFlow(1)
        val received = mutableListOf<Int>()
        val subscription = source.subscribe(testDispatcher) { received += it }
        advanceUntilIdle()

        subscription.cancel()
        source.value = 2
        advanceUntilIdle()

        assertEquals(listOf(1), received)
    }

    @Test
    fun `case - GIVEN a subscription WHEN cancelled THEN isCancelled flips to true`() = runTest(testDispatcher) {
        val subscription = MutableStateFlow(0).subscribe(testDispatcher) { }
        advanceUntilIdle()
        assertFalse(subscription.isCancelled)

        subscription.cancel()

        assertTrue(subscription.isCancelled)
    }
}
