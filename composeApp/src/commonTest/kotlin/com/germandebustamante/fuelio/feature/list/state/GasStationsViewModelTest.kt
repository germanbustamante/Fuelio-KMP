package com.germandebustamante.fuelio.feature.list.state

import app.cash.turbine.test
import com.germandebustamante.fuelio.core.domain.error.testing.DomainErrorMother
import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
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

    private val testDispatcher = StandardTestDispatcher()

    private val getGasStationsByLocationUseCase: GetGasStationsByLocationUseCase = mock {
        every { invoke(any()) } returns flowOf(Result.success(GasStationBOMother.gasStationBOList()))
    }

    private val getProvincesUseCase: GetProvincesUseCase = mock {
        every { invoke() } returns flowOf(Result.success(ProvinceBOMother.provinceBOList()))
    }

    private val locationPermissionController: LocationPermissionController = mock {
        everySuspend { requestPermission() } returns LocationPermissionState.Granted
        everySuspend { checkCurrentStatus() } returns LocationPermissionState.DeniedAlways
        everySuspend { getCurrentLocation() } returns null
        every { openAppSettings() } returns Unit
    }

    private val resolveProvinceByLocationUseCase = ResolveProvinceByLocationUseCase()

    private lateinit var sut: GasStationsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    //region Init

    @Test
    fun `init - GIVEN gas stations and provinces load success WHEN ViewModel initialized THEN both use cases are invoked`() =
        runTest {
            // GIVEN
            // default mocks return success

            // WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            verify { getProvincesUseCase() }
            verify { getGasStationsByLocationUseCase(ProvinceBOMother.provinceBO().id) }
        }

    @Test
    fun `init - GIVEN gas stations and provinces load success WHEN ViewModel initialized THEN first province is selected and gas stations are loaded`() =
        runTest {
            // GIVEN
            // default mocks return success

            // WHEN
            createSut()
            advanceUntilIdle()

            // THEN
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
            // GIVEN
            // default mocks return success

            // WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertFalse(awaitItem().isLoading)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `init - GIVEN gas stations and provinces load success WHEN ViewModel initialized THEN provinces list is populated`() =
        runTest {
            // GIVEN
            // default mocks return success

            // WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertEquals(ProvinceBOMother.provinceBOList(), awaitItem().provinces)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `init - GIVEN gas stations returns error WHEN ViewModel initialized THEN error is notified`() =
        runTest {
            // GIVEN
            val error = DomainErrorMother.serverError()
            stubGasStationsFailure(error)

            // WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertEquals(error, awaitItem().error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `init - GIVEN provinces returns error WHEN ViewModel initialized THEN error is notified`() =
        runTest {
            // GIVEN
            val error = DomainErrorMother.serverError()
            stubProvincesFailure(error)

            // WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertEquals(error, awaitItem().error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `init - GIVEN network unavailable WHEN ViewModel initialized THEN network error is notified`() =
        runTest {
            // GIVEN
            val error = DomainErrorMother.networkUnavailable()
            stubProvincesFailure(error)

            // WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertEquals(error, awaitItem().error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `init - WHEN ViewModel initialized THEN showPermissionDeniedPermanentlySnackbar is false`() =
        runTest {
            // GIVEN / WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertFalse(awaitItem().showPermissionDeniedPermanentlySnackbar)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `init - GIVEN location already granted WHEN ViewModel initialized THEN province is resolved from location silently`() =
        runTest {
            // GIVEN
            val targetProvince = ProvinceBOMother.provinceBOList()[SECOND_PROVINCE_INDEX]
            stubLocationGrantedWithProvince(targetProvince.name)

            // WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                val state = awaitItem()
                assertEquals(targetProvince, state.selectedProvince)
                assertFalse(state.showPermissionDeniedPermanentlySnackbar)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `init - GIVEN permission NotDetermined WHEN ViewModel initialized THEN permission is requested and if granted location is used`() =
        runTest {
            // GIVEN
            val targetProvince = ProvinceBOMother.provinceBOList()[SECOND_PROVINCE_INDEX]
            stubPermissionNotDeterminedThenGranted(targetProvince.name)

            // WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertEquals(targetProvince, awaitItem().selectedProvince)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `init - GIVEN permission Denied WHEN ViewModel initialized THEN permission is re-requested and if granted location is used`() =
        runTest {
            // GIVEN
            val targetProvince = ProvinceBOMother.provinceBOList()[SECOND_PROVINCE_INDEX]
            stubPermissionDeniedThenGranted(targetProvince.name)

            // WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertEquals(targetProvince, awaitItem().selectedProvince)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `init - GIVEN permission DeniedAlways WHEN ViewModel initialized THEN location is not fetched`() =
        runTest {
            // GIVEN / WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            verifySuspend(dev.mokkery.verify.VerifyMode.not) { locationPermissionController.getCurrentLocation() }
        }

    //endregion

    //region onDetectLocationTapped

    @Test
    fun `onDetectLocationTapped - GIVEN location matches a province WHEN tapped THEN matching province is selected`() =
        runTest {
            // GIVEN
            val targetProvince = ProvinceBOMother.provinceBOList()[SECOND_PROVINCE_INDEX]
            stubCurrentLocation(targetProvince.name)
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onDetectLocationTapped()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertEquals(targetProvince, awaitItem().selectedProvince)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onDetectLocationTapped - GIVEN location does not match any province WHEN tapped THEN first province is selected`() =
        runTest {
            // GIVEN
            stubCurrentLocation(UNKNOWN_PROVINCE)
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onDetectLocationTapped()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertEquals(ProvinceBOMother.provinceBOList().first(), awaitItem().selectedProvince)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onDetectLocationTapped - GIVEN location is null WHEN tapped THEN first province is selected`() =
        runTest {
            // GIVEN
            stubNullLocation()
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onDetectLocationTapped()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertEquals(ProvinceBOMother.provinceBOList().first(), awaitItem().selectedProvince)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onDetectLocationTapped - GIVEN permission denied WHEN tapped THEN snackbar is not shown`() =
        runTest {
            // GIVEN
            stubPermissionRequestResult(LocationPermissionState.Denied)
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onDetectLocationTapped()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertFalse(awaitItem().showPermissionDeniedPermanentlySnackbar)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onDetectLocationTapped - GIVEN permission denied always WHEN tapped THEN snackbar is shown`() =
        runTest {
            // GIVEN
            stubPermissionRequestResult(LocationPermissionState.DeniedAlways)
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onDetectLocationTapped()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertTrue(awaitItem().showPermissionDeniedPermanentlySnackbar)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region onProvinceSelected

    @Test
    fun `onProvinceSelected - GIVEN provinces loaded WHEN different province selected THEN gas stations are fetched for new province`() =
        runTest {
            // GIVEN
            val secondProvince = ProvinceBOMother.provinceBOList()[SECOND_PROVINCE_INDEX]
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onProvinceSelected(secondProvince)
            advanceUntilIdle()

            // THEN
            verify { getGasStationsByLocationUseCase(secondProvince.id) }
        }

    @Test
    fun `onProvinceSelected - GIVEN provinces loaded WHEN different province selected THEN selectedProvince is updated in state`() =
        runTest {
            // GIVEN
            val secondProvince = ProvinceBOMother.provinceBOList()[SECOND_PROVINCE_INDEX]
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onProvinceSelected(secondProvince)
            advanceUntilIdle()

            // THEN
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
            // GIVEN
            stubGasStationsFailure(DomainErrorMother.serverError())
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onDismissError()

            // THEN
            sut.state.test {
                assertNull(awaitItem().error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region onOpenAppSettings

    @Test
    fun `onOpenAppSettings - WHEN called THEN openAppSettings is invoked and snackbar is dismissed`() =
        runTest {
            // GIVEN
            stubPermissionRequestResult(LocationPermissionState.DeniedAlways)
            createSut()
            advanceUntilIdle()
            sut.onDetectLocationTapped()
            advanceUntilIdle()

            // WHEN
            sut.onOpenAppSettings()

            // THEN
            verify { locationPermissionController.openAppSettings() }
            sut.state.test {
                assertFalse(awaitItem().showPermissionDeniedPermanentlySnackbar)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region onFuelFilterSelected

    @Test
    fun `onFuelFilterSelected - GIVEN default state WHEN ViewModel initialized THEN selectedFuelFilter is Gasoline95`() =
        runTest {
            // GIVEN / WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertEquals(FuelFilter.Gasoline95, awaitItem().selectedFuelFilter)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onFuelFilterSelected - GIVEN Gasoline95 selected WHEN Diesel selected THEN selectedFuelFilter is Diesel`() =
        runTest {
            // GIVEN
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onFuelFilterSelected(FuelFilter.Diesel)
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertEquals(FuelFilter.Diesel, awaitItem().selectedFuelFilter)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onFuelFilterSelected - GIVEN gas stations loaded WHEN filter changed THEN all items have updated fuelFilter`() =
        runTest {
            // GIVEN
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onFuelFilterSelected(FuelFilter.Gasoline98)
            advanceUntilIdle()

            // THEN
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
            // GIVEN
            val secondProvince = ProvinceBOMother.provinceBOList()[SECOND_PROVINCE_INDEX]
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onFuelFilterSelected(FuelFilter.DieselPremium)
            advanceUntilIdle()
            sut.onProvinceSelected(secondProvince)
            advanceUntilIdle()

            // THEN
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
            // GIVEN
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onFilterProvinceToggle(true)

            // THEN
            sut.state.test {
                assertTrue(awaitItem().showFilterProvince)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onFilterProvinceToggle - GIVEN filter visible WHEN toggle called with false THEN showFilterProvince is false`() =
        runTest {
            // GIVEN
            createSut()
            advanceUntilIdle()
            sut.onFilterProvinceToggle(true)

            // WHEN
            sut.onFilterProvinceToggle(false)

            // THEN
            sut.state.test {
                assertFalse(awaitItem().showFilterProvince)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region onSearchQueryChanged

    @Test
    fun `onSearchQueryChanged - GIVEN default state WHEN ViewModel initialized THEN searchQuery is empty`() =
        runTest {
            // GIVEN / WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertEquals("", awaitItem().searchQuery)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onSearchQueryChanged - GIVEN stations loaded WHEN query matches station name THEN only matching stations returned`() =
        runTest {
            // GIVEN
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onSearchQueryChanged(MATCHING_NAME_QUERY)
            advanceUntilIdle()

            // THEN
            sut.state.test {
                val stations = awaitItem().gasStations
                assertEquals(1, stations.size)
                assertTrue(stations.first().station.name.contains(MATCHING_NAME_QUERY, ignoreCase = true))
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onSearchQueryChanged - GIVEN stations loaded WHEN query matches address THEN only matching stations returned`() =
        runTest {
            // GIVEN
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onSearchQueryChanged(MATCHING_ADDRESS_QUERY)
            advanceUntilIdle()

            // THEN
            sut.state.test {
                val stations = awaitItem().gasStations
                assertTrue(stations.isNotEmpty())
                assertTrue(stations.all { it.station.getFullDirection().contains(MATCHING_ADDRESS_QUERY, ignoreCase = true) })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onSearchQueryChanged - GIVEN stations loaded WHEN query matches nothing THEN empty list returned`() =
        runTest {
            // GIVEN
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onSearchQueryChanged(NO_MATCH_QUERY)
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertTrue(awaitItem().gasStations.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onSearchQueryChanged - GIVEN active query WHEN query cleared THEN full list restored`() =
        runTest {
            // GIVEN
            createSut()
            advanceUntilIdle()
            sut.onSearchQueryChanged(MATCHING_NAME_QUERY)
            advanceUntilIdle()

            // WHEN
            sut.onSearchQueryChanged("")
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertEquals(GasStationBOMother.gasStationBOList().size, awaitItem().gasStations.size)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onSearchQueryChanged - GIVEN active fuel filter WHEN query applied THEN both filters combined`() =
        runTest {
            // GIVEN
            createSut()
            advanceUntilIdle()
            sut.onFuelFilterSelected(FuelFilter.Diesel)
            advanceUntilIdle()

            // WHEN
            sut.onSearchQueryChanged(MATCHING_NAME_QUERY)
            advanceUntilIdle()

            // THEN
            sut.state.test {
                val stations = awaitItem().gasStations
                assertEquals(1, stations.size)
                assertTrue(stations.all { it.fuelFilter == FuelFilter.Diesel })
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region isContentReady

    @Test
    fun `isContentReady - GIVEN initial state WHEN ViewModel initialized THEN isContentReady is false`() =
        runTest {
            // GIVEN / WHEN
            createSut()

            // THEN
            sut.state.test {
                assertFalse(awaitItem().isContentReady())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `isContentReady - GIVEN stations loaded WHEN search empties list THEN isContentReady remains true`() =
        runTest {
            // GIVEN
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onSearchQueryChanged(NO_MATCH_QUERY)
            advanceUntilIdle()

            // THEN
            sut.state.test {
                val state = awaitItem()
                assertTrue(state.gasStations.isEmpty())
                assertTrue(state.isContentReady())
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region contentState

    @Test
    fun `contentState - GIVEN initial state WHEN ViewModel initialized THEN contentState is Loading`() =
        runTest {
            // GIVEN / WHEN
            createSut()

            // THEN
            sut.state.test {
                assertTrue(awaitItem().contentState is ContentState.Loading)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `contentState - GIVEN stations loaded WHEN data available THEN contentState is Success`() =
        runTest {
            // GIVEN / WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertTrue(awaitItem().contentState is ContentState.Success)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `contentState - GIVEN stations loaded WHEN search returns empty list THEN contentState is Empty`() =
        runTest {
            // GIVEN
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onSearchQueryChanged(NO_MATCH_QUERY)
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertTrue(awaitItem().contentState is ContentState.Empty)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `contentState - GIVEN gas stations returns error WHEN error notified THEN contentState is Error`() =
        runTest {
            // GIVEN
            stubGasStationsFailure(DomainErrorMother.serverError())

            // WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertTrue(awaitItem().contentState is ContentState.Error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region onRetry

    @Test
    fun `onRetry - GIVEN error in state WHEN called THEN error is cleared`() =
        runTest {
            // GIVEN
            stubGasStationsFailure(DomainErrorMother.serverError())
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onRetry()

            // THEN
            sut.state.test {
                assertNull(awaitItem().error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region onRefresh

    @Test
    fun `onRefresh - GIVEN stations loaded WHEN called THEN stations are re-fetched and state updated`() =
        runTest {
            // GIVEN
            val refreshedStation = GasStationBOMother.gasStationBO(id = REFRESHED_STATION_ID, name = REFRESHED_STATION_NAME)
            createSut()
            advanceUntilIdle()

            // WHEN
            stubGasStationsSuccess(listOf(refreshedStation))
            sut.onRefresh()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertEquals(listOf(refreshedStation), awaitItem().gasStations.map { it.station })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onRefresh - GIVEN stations loaded WHEN refresh fails THEN error is notified`() =
        runTest {
            // GIVEN
            val error = DomainErrorMother.serverError()
            createSut()
            advanceUntilIdle()

            // WHEN
            stubGasStationsFailure(error)
            sut.onRefresh()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertEquals(error, awaitItem().error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onRefresh - GIVEN stations loaded WHEN refresh completes THEN isRefreshing is false`() =
        runTest {
            // GIVEN
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onRefresh()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertFalse(awaitItem().isRefreshing)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onRefresh - GIVEN active fuel filter WHEN refresh called THEN refreshed stations use active filter`() =
        runTest {
            // GIVEN
            createSut()
            advanceUntilIdle()
            sut.onFuelFilterSelected(FuelFilter.Diesel)
            advanceUntilIdle()

            // WHEN
            sut.onRefresh()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                val state = awaitItem()
                assertEquals(FuelFilter.Diesel, state.selectedFuelFilter)
                assertTrue(state.gasStations.all { it.fuelFilter == FuelFilter.Diesel })
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region onToggleFavorite

    @Test
    fun `onToggleFavorite - GIVEN station not in favorites WHEN toggled THEN station is added to favorites`() =
        runTest {
            // GIVEN
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onToggleFavorite(STATION_ID_1)

            // THEN
            sut.state.test {
                assertTrue(STATION_ID_1 in awaitItem().favorites)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onToggleFavorite - GIVEN station in favorites WHEN toggled again THEN station is removed from favorites`() =
        runTest {
            // GIVEN
            createSut()
            advanceUntilIdle()
            sut.onToggleFavorite(STATION_ID_1)

            // WHEN
            sut.onToggleFavorite(STATION_ID_1)

            // THEN
            sut.state.test {
                assertFalse(STATION_ID_1 in awaitItem().favorites)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region onDetectLocationTapped - NotDetermined

    @Test
    fun `onDetectLocationTapped - GIVEN permission NotDetermined WHEN tapped THEN snackbar is not shown`() =
        runTest {
            // GIVEN
            stubPermissionRequestResult(LocationPermissionState.NotDetermined)
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onDetectLocationTapped()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertFalse(awaitItem().showPermissionDeniedPermanentlySnackbar)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region onDismissPermissionSnackbar

    @Test
    fun `onDismissPermissionSnackbar - GIVEN snackbar showing WHEN called THEN showPermissionDeniedPermanentlySnackbar is false`() =
        runTest {
            // GIVEN
            stubPermissionRequestResult(LocationPermissionState.DeniedAlways)
            createSut()
            advanceUntilIdle()
            sut.onDetectLocationTapped()
            advanceUntilIdle()

            // WHEN
            sut.onDismissPermissionSnackbar()

            // THEN
            sut.state.test {
                assertFalse(awaitItem().showPermissionDeniedPermanentlySnackbar)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region markCheapest

    @Test
    fun `markCheapest - GIVEN stations with same price WHEN loaded THEN only first station is marked cheapest`() =
        runTest {
            // GIVEN / WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                val stations = awaitItem().gasStations
                assertEquals(2, stations.size)
                assertTrue(stations[0].isCheapest)
                assertFalse(stations[1].isCheapest)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `markCheapest - GIVEN stations with different prices WHEN loaded THEN station with lowest price is marked cheapest`() =
        runTest {
            // GIVEN
            val cheapStation = GasStationBOMother.gasStationBO(id = STATION_ID_1, gasolinePrice95 = CHEAP_GASOLINE_PRICE)
            val expensiveStation = GasStationBOMother.gasStationBO(id = STATION_ID_2, gasolinePrice95 = EXPENSIVE_GASOLINE_PRICE)
            stubGasStationsSuccess(listOf(cheapStation, expensiveStation))

            // WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                val stations = awaitItem().gasStations
                assertTrue(stations.first { it.station.id == STATION_ID_1 }.isCheapest)
                assertFalse(stations.first { it.station.id == STATION_ID_2 }.isCheapest)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `markCheapest - GIVEN no station has price for selected fuel WHEN loaded THEN no station is marked cheapest`() =
        runTest {
            // GIVEN
            stubGasStationsSuccess(listOf(
                GasStationBOMother.gasStationBO(id = STATION_ID_1, gasolinePrice95 = null),
                GasStationBOMother.gasStationBO(id = STATION_ID_2, gasolinePrice95 = null),
            ))

            // WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertTrue(awaitItem().gasStations.none { it.isCheapest })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `markCheapest - GIVEN stations with different diesel prices WHEN Diesel filter selected THEN cheapest recalculated for Diesel`() =
        runTest {
            // GIVEN
            val cheapDiesel = GasStationBOMother.gasStationBO(id = STATION_ID_1, dieselPrice = CHEAP_DIESEL_PRICE, gasolinePrice95 = EXPENSIVE_GASOLINE_PRICE)
            val expensiveDiesel = GasStationBOMother.gasStationBO(id = STATION_ID_2, dieselPrice = EXPENSIVE_DIESEL_PRICE, gasolinePrice95 = CHEAP_GASOLINE_PRICE)
            stubGasStationsSuccess(listOf(cheapDiesel, expensiveDiesel))
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onFuelFilterSelected(FuelFilter.Diesel)
            advanceUntilIdle()

            // THEN
            sut.state.test {
                val stations = awaitItem().gasStations
                assertTrue(stations.first { it.station.id == STATION_ID_1 }.isCheapest)
                assertFalse(stations.first { it.station.id == STATION_ID_2 }.isCheapest)
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region distanceInKilometers

    @Test
    fun `distanceInKilometers - GIVEN no user location WHEN stations loaded THEN distance is null for all stations`() =
        runTest {
            // GIVEN / WHEN
            createSut()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertTrue(awaitItem().gasStations.all { it.distanceInKilometers == null })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `distanceInKilometers - GIVEN user location granted WHEN location detected THEN stations have distance set`() =
        runTest {
            // GIVEN
            stubCurrentLocation(TEST_PROVINCE)
            createSut()
            advanceUntilIdle()

            // WHEN
            sut.onDetectLocationTapped()
            advanceUntilIdle()

            // THEN
            sut.state.test {
                assertTrue(awaitItem().gasStations.all { it.distanceInKilometers != null })
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion

    //region Stubs

    private fun stubLocationGrantedWithProvince(province: String) {
        everySuspend { locationPermissionController.checkCurrentStatus() } returns LocationPermissionState.Granted
        everySuspend { locationPermissionController.getCurrentLocation() } returns locationOf(province)
    }

    private fun stubPermissionNotDeterminedThenGranted(province: String) {
        everySuspend { locationPermissionController.checkCurrentStatus() } returns LocationPermissionState.NotDetermined
        everySuspend { locationPermissionController.requestPermission() } returns LocationPermissionState.Granted
        everySuspend { locationPermissionController.getCurrentLocation() } returns locationOf(province)
    }

    private fun stubPermissionDeniedThenGranted(province: String) {
        everySuspend { locationPermissionController.checkCurrentStatus() } returns LocationPermissionState.Denied
        everySuspend { locationPermissionController.requestPermission() } returns LocationPermissionState.Granted
        everySuspend { locationPermissionController.getCurrentLocation() } returns locationOf(province)
    }

    private fun stubPermissionRequestResult(state: LocationPermissionState) {
        everySuspend { locationPermissionController.requestPermission() } returns state
    }

    private fun stubCurrentLocation(province: String) {
        everySuspend { locationPermissionController.getCurrentLocation() } returns locationOf(province)
    }

    private fun stubNullLocation() {
        everySuspend { locationPermissionController.getCurrentLocation() } returns null
    }

    private fun stubGasStationsFailure(error: Throwable) {
        every { getGasStationsByLocationUseCase(any()) } returns flowOf(Result.failure(error))
    }

    private fun stubGasStationsSuccess(stations: List<GasStationBO>) {
        every { getGasStationsByLocationUseCase(any()) } returns flowOf(Result.success(stations))
    }

    private fun stubProvincesFailure(error: Throwable) {
        every { getProvincesUseCase() } returns flowOf(Result.failure(error))
    }

    private fun locationOf(province: String) =
        LocationPermissionController.Location(province = province, latitude = TEST_LATITUDE, longitude = TEST_LONGITUDE)

    //endregion

    private fun createSut() {
        sut = GasStationsViewModel(
            getGasStationByLocationUseCase = getGasStationsByLocationUseCase,
            getProvincesUseCase = getProvincesUseCase,
            locationPermissionController = locationPermissionController,
            resolveProvinceByLocationUseCase = resolveProvinceByLocationUseCase,
            defaultDispatcher = testDispatcher,
        )
    }

    companion object {
        private const val SECOND_PROVINCE_INDEX = 1
        private const val MATCHING_NAME_QUERY = "Gasoil"
        private const val MATCHING_ADDRESS_QUERY = "Calle Principal"
        private const val NO_MATCH_QUERY = "xyznotexistent"
        private const val UNKNOWN_PROVINCE = "Tokio"
        private const val TEST_PROVINCE = "Madrid"
        private const val TEST_LATITUDE = 40.5
        private const val TEST_LONGITUDE = -3.6
        private const val STATION_ID_1 = "1"
        private const val STATION_ID_2 = "2"
        private const val REFRESHED_STATION_ID = "99"
        private const val REFRESHED_STATION_NAME = "Refreshed Station"
        private const val CHEAP_GASOLINE_PRICE = 1.40
        private const val EXPENSIVE_GASOLINE_PRICE = 1.75
        private const val CHEAP_DIESEL_PRICE = 1.30
        private const val EXPENSIVE_DIESEL_PRICE = 1.60
    }
}
