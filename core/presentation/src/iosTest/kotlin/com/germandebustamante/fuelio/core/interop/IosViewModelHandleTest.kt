package com.germandebustamante.fuelio.core.interop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class IosViewModelHandleTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `case - GIVEN a factory lambda WHEN a handle is created THEN it exposes that very instance`() = runTest(testDispatcher) {
        val expected = SpyViewModel()

        val handle = createViewModelHandle(SpyViewModel::class) { expected }

        assertSame(expected, handle.viewModel)
    }

    @Test
    fun `case - GIVEN an open handle WHEN close is called THEN the view model is cleared and its scope cancelled`() = runTest(testDispatcher) {
        val handle = createViewModelHandle(SpyViewModel::class) { SpyViewModel() }
        val viewModel = handle.viewModel
        assertTrue(viewModel.isScopeActive, "viewModelScope should be active before close()")

        handle.close()

        assertTrue(viewModel.cleared, "onCleared() should have run")
        assertFalse(viewModel.isScopeActive, "viewModelScope should be cancelled after close()")
    }
}

private class SpyViewModel : ViewModel() {

    var cleared: Boolean = false
        private set

    val isScopeActive: Boolean
        get() = viewModelScope.isActive

    override fun onCleared() {
        cleared = true
    }
}
