package com.germandebustamante.fuelio.feature.list.state

import app.cash.turbine.test
import com.germandebustamante.fuelio.core.domain.error.testing.DomainErrorMother
import com.germandebustamante.fuelio.core.domain.gasstation.testing.GasStationBOMother
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationsByLocationUseCase
import com.germandebustamante.fuelio.core.domain.province.testing.ProvinceBOMother
import com.germandebustamante.fuelio.core.domain.province.usecase.GetProvincesUseCase
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GasStationsViewModelTest {

    private val getGasStationsByLocationUseCase: GetGasStationsByLocationUseCase = mock {
        every { invoke(any()) } returns flowOf(Result.success(GasStationBOMother.gasStationBOList()))
    }

    private val getProvincesUseCase: GetProvincesUseCase = mock {
        every { invoke() } returns flowOf(Result.success(ProvinceBOMother.provinceBOList()))
    }

    private lateinit var sut: GasStationsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    //region Init

    @Test
    fun `init - GIVEN gas stations and provinces load success WHEN ViewModel initialized THEN both use cases are invoked`() =
        runTest {
            createSut()
            advanceUntilIdle()

            verify { getProvincesUseCase() }
            verify { getGasStationsByLocationUseCase(ProvinceBOMother.provinceBO().id) }
        }

    @Test
    fun `init - GIVEN gas stations and provinces load success WHEN ViewModel initialized THEN first province is selected and gas stations are loaded`() =
        runTest {
            createSut()
            advanceUntilIdle()

            sut.state.test {
                val state = awaitItem()
                assertEquals(ProvinceBOMother.provinceBOList().first(), state.selectedProvince)
                assertEquals(GasStationBOMother.gasStationBOList(), state.gasStations.map { it.station })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `init - GIVEN gas stations and provinces load success WHEN ViewModel initialized THEN isLoading is false`() =
        runTest {
            createSut()
            advanceUntilIdle()

            sut.state.test {
                assertFalse(awaitItem().isLoading)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `init - GIVEN gas stations and provinces load success WHEN ViewModel initialized THEN provinces list is populated`() =
        runTest {
            createSut()
            advanceUntilIdle()

            sut.state.test {
                assertEquals(ProvinceBOMother.provinceBOList(), awaitItem().provinces)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `init - GIVEN gas stations returns error WHEN ViewModel initialized THEN error is notified`() =
        runTest {
            val error = DomainErrorMother.serverError()
            every { getGasStationsByLocationUseCase(any()) } returns flowOf(Result.failure(error))

            createSut()
            advanceUntilIdle()

            sut.state.test {
                assertEquals(error, awaitItem().error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `init - GIVEN provinces returns error WHEN ViewModel initialized THEN error is notified`() =
        runTest {
            val error = DomainErrorMother.serverError()
            every { getProvincesUseCase() } returns flowOf(Result.failure(error))

            createSut()
            advanceUntilIdle()

            sut.state.test {
                assertEquals(error, awaitItem().error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `init - GIVEN network unavailable WHEN ViewModel initialized THEN network error is notified`() =
        runTest {
            val error = DomainErrorMother.networkUnavailable()
            every { getProvincesUseCase() } returns flowOf(Result.failure(error))

            createSut()
            advanceUntilIdle()

            sut.state.test {
                assertEquals(error, awaitItem().error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region onProvinceSelected

    @Test
    fun `onProvinceSelected - GIVEN provinces loaded WHEN different province selected THEN gas stations are fetched for new province`() =
        runTest {
            val secondProvince = ProvinceBOMother.provinceBOList()[1]
            createSut()
            advanceUntilIdle()

            sut.onProvinceSelected(secondProvince)
            advanceUntilIdle()

            verify { getGasStationsByLocationUseCase(secondProvince.id) }
        }

    @Test
    fun `onProvinceSelected - GIVEN provinces loaded WHEN different province selected THEN selectedProvince is updated in state`() =
        runTest {
            val secondProvince = ProvinceBOMother.provinceBOList()[1]
            createSut()
            advanceUntilIdle()

            sut.onProvinceSelected(secondProvince)
            advanceUntilIdle()

            sut.state.test {
                assertEquals(secondProvince, awaitItem().selectedProvince)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region onDismissError

    @Test
    fun `onDismissError - GIVEN error in state WHEN onDismissError called THEN error is cleared`() =
        runTest {
            every { getGasStationsByLocationUseCase(any()) } returns flowOf(
                Result.failure(DomainErrorMother.serverError())
            )
            createSut()
            advanceUntilIdle()

            sut.onDismissError()

            sut.state.test {
                assertNull(awaitItem().error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region onFilterProvinceToggle

    @Test
    fun `onFilterProvinceToggle - GIVEN default state WHEN toggle called with true THEN showFilterProvince is true`() =
        runTest {
            createSut()
            advanceUntilIdle()

            sut.onFilterProvinceToggle(true)

            sut.state.test {
                assertTrue(awaitItem().showFilterProvince)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onFilterProvinceToggle - GIVEN filter visible WHEN toggle called with false THEN showFilterProvince is false`() =
        runTest {
            createSut()
            advanceUntilIdle()
            sut.onFilterProvinceToggle(true)

            sut.onFilterProvinceToggle(false)

            sut.state.test {
                assertFalse(awaitItem().showFilterProvince)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    private fun createSut() {
        sut = GasStationsViewModel(
            getGasStationByLocationUseCase = getGasStationsByLocationUseCase,
            getProvincesUseCase = getProvincesUseCase
        )
    }
}
