package com.germandebustamante.fuelio.feature.list.state

import app.cash.turbine.test
import com.germandebustamante.fuelio.core.domain.error.testing.DomainErrorMother
import com.germandebustamante.fuelio.core.domain.gasstation.testing.GasStationBOMother
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationsByLocationUseCase
import com.germandebustamante.fuelio.core.domain.province.testing.ProvinceBOMother
import com.germandebustamante.fuelio.core.domain.province.usecase.GetProvincesUseCase
import com.germandebustamante.fuelio.core.domain.province.usecase.ResolveProvinceByLocationUseCase
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionState
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
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

    private val locationPermissionController: LocationPermissionController = mock {
        everySuspend { requestPermission() } returns LocationPermissionState.Granted
        everySuspend { checkCurrentStatus() } returns LocationPermissionState.Granted
        everySuspend { getCurrentLocation() } returns null
        every { openAppSettings() } returns Unit
    }

    private val resolveProvinceByLocationUseCase = ResolveProvinceByLocationUseCase()

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

    @Test
    fun `init - WHEN ViewModel initialized THEN locationPermissionState is null`() =
        runTest {
            createSut()
            advanceUntilIdle()

            sut.state.test {
                assertNull(awaitItem().locationPermissionState)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region onDetectLocationTapped

    @Test
    fun `onDetectLocationTapped - GIVEN location matches a province WHEN tapped THEN matching province is selected`() =
        runTest {
            val targetProvince = ProvinceBOMother.provinceBOList()[1]
            everySuspend { locationPermissionController.getCurrentLocation() } returns
                LocationPermissionController.Location(province = targetProvince.name, latitude = 0.0, longitude = 0.0)
            createSut()
            advanceUntilIdle()

            sut.onDetectLocationTapped()
            advanceUntilIdle()

            sut.state.test {
                assertEquals(targetProvince, awaitItem().selectedProvince)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onDetectLocationTapped - GIVEN location does not match any province WHEN tapped THEN first province is selected`() =
        runTest {
            everySuspend { locationPermissionController.getCurrentLocation() } returns
                LocationPermissionController.Location(province = "Tokio", latitude = 0.0, longitude = 0.0)
            createSut()
            advanceUntilIdle()

            sut.onDetectLocationTapped()
            advanceUntilIdle()

            sut.state.test {
                assertEquals(ProvinceBOMother.provinceBOList().first(), awaitItem().selectedProvince)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onDetectLocationTapped - GIVEN location is null WHEN tapped THEN first province is selected`() =
        runTest {
            everySuspend { locationPermissionController.getCurrentLocation() } returns null
            createSut()
            advanceUntilIdle()

            sut.onDetectLocationTapped()
            advanceUntilIdle()

            sut.state.test {
                assertEquals(ProvinceBOMother.provinceBOList().first(), awaitItem().selectedProvince)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onDetectLocationTapped - GIVEN permission denied WHEN tapped THEN locationPermissionState is Denied`() =
        runTest {
            everySuspend { locationPermissionController.requestPermission() } returns LocationPermissionState.Denied
            createSut()
            advanceUntilIdle()

            sut.onDetectLocationTapped()
            advanceUntilIdle()

            sut.state.test {
                assertEquals(LocationPermissionState.Denied, awaitItem().locationPermissionState)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onDetectLocationTapped - GIVEN permission denied always WHEN tapped THEN locationPermissionState is DeniedAlways`() =
        runTest {
            everySuspend { locationPermissionController.requestPermission() } returns LocationPermissionState.DeniedAlways
            createSut()
            advanceUntilIdle()

            sut.onDetectLocationTapped()
            advanceUntilIdle()

            sut.state.test {
                assertEquals(LocationPermissionState.DeniedAlways, awaitItem().locationPermissionState)
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

    //region onPermissionRationaleAccepted

    @Test
    fun `onPermissionRationaleAccepted - WHEN re-request granted THEN locationPermissionState is null`() =
        runTest {
            everySuspend { locationPermissionController.requestPermission() } returns LocationPermissionState.Denied
            createSut()
            advanceUntilIdle()
            sut.onDetectLocationTapped()
            advanceUntilIdle()

            everySuspend { locationPermissionController.requestPermission() } returns LocationPermissionState.Granted
            sut.onPermissionRationaleAccepted()
            advanceUntilIdle()

            sut.state.test {
                assertNull(awaitItem().locationPermissionState)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onPermissionRationaleAccepted - WHEN re-request granted THEN province resolved from location`() =
        runTest {
            val targetProvince = ProvinceBOMother.provinceBOList()[1]
            everySuspend { locationPermissionController.requestPermission() } returns LocationPermissionState.Denied
            createSut()
            advanceUntilIdle()
            sut.onDetectLocationTapped()
            advanceUntilIdle()

            everySuspend { locationPermissionController.requestPermission() } returns LocationPermissionState.Granted
            everySuspend { locationPermissionController.getCurrentLocation() } returns
                LocationPermissionController.Location(province = targetProvince.name, latitude = 0.0, longitude = 0.0)
            sut.onPermissionRationaleAccepted()
            advanceUntilIdle()

            sut.state.test {
                assertEquals(targetProvince, awaitItem().selectedProvince)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onPermissionRationaleAccepted - WHEN re-request denied always THEN locationPermissionState is DeniedAlways`() =
        runTest {
            everySuspend { locationPermissionController.requestPermission() } returns LocationPermissionState.Denied
            createSut()
            advanceUntilIdle()
            sut.onDetectLocationTapped()
            advanceUntilIdle()

            everySuspend { locationPermissionController.requestPermission() } returns LocationPermissionState.DeniedAlways
            sut.onPermissionRationaleAccepted()
            advanceUntilIdle()

            sut.state.test {
                assertEquals(LocationPermissionState.DeniedAlways, awaitItem().locationPermissionState)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region onPermissionDialogDismissed

    @Test
    fun `onPermissionDialogDismissed - GIVEN permission denied state WHEN dismissed THEN locationPermissionState is null`() =
        runTest {
            everySuspend { locationPermissionController.requestPermission() } returns LocationPermissionState.Denied
            createSut()
            advanceUntilIdle()
            sut.onDetectLocationTapped()
            advanceUntilIdle()

            sut.onPermissionDialogDismissed()

            sut.state.test {
                assertNull(awaitItem().locationPermissionState)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region onOpenAppSettings

    @Test
    fun `onOpenAppSettings - WHEN called THEN openAppSettings is invoked and locationPermissionState is null`() =
        runTest {
            everySuspend { locationPermissionController.requestPermission() } returns LocationPermissionState.DeniedAlways
            createSut()
            advanceUntilIdle()
            sut.onDetectLocationTapped()
            advanceUntilIdle()

            sut.onOpenAppSettings()

            verify { locationPermissionController.openAppSettings() }
            sut.state.test {
                assertNull(awaitItem().locationPermissionState)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region onFuelFilterSelected

    @Test
    fun `onFuelFilterSelected - GIVEN default state WHEN ViewModel initialized THEN selectedFuelFilter is Gasoline95`() =
        runTest {
            createSut()
            advanceUntilIdle()

            sut.state.test {
                assertEquals(FuelFilter.Gasoline95, awaitItem().selectedFuelFilter)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onFuelFilterSelected - GIVEN Gasoline95 selected WHEN Diesel selected THEN selectedFuelFilter is Diesel`() =
        runTest {
            createSut()
            advanceUntilIdle()

            sut.onFuelFilterSelected(FuelFilter.Diesel)

            sut.state.test {
                assertEquals(FuelFilter.Diesel, awaitItem().selectedFuelFilter)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onFuelFilterSelected - GIVEN gas stations loaded WHEN filter changed THEN all items have updated fuelFilter`() =
        runTest {
            createSut()
            advanceUntilIdle()

            sut.onFuelFilterSelected(FuelFilter.Gasoline98)

            sut.state.test {
                val stations = awaitItem().gasStations
                assertTrue(stations.isNotEmpty())
                assertTrue(stations.all { it.fuelFilter == FuelFilter.Gasoline98 })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onFuelFilterSelected - GIVEN filter changed WHEN new province is selected THEN new stations inherit active filter`() =
        runTest {
            val secondProvince = ProvinceBOMother.provinceBOList()[1]
            createSut()
            advanceUntilIdle()

            sut.onFuelFilterSelected(FuelFilter.DieselPremium)
            sut.onProvinceSelected(secondProvince)
            advanceUntilIdle()

            sut.state.test {
                val stations = awaitItem().gasStations
                assertTrue(stations.isNotEmpty())
                assertTrue(stations.all { it.fuelFilter == FuelFilter.DieselPremium })
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
            getProvincesUseCase = getProvincesUseCase,
            locationPermissionController = locationPermissionController,
            resolveProvinceByLocationUseCase = resolveProvinceByLocationUseCase,
        )
    }
}
