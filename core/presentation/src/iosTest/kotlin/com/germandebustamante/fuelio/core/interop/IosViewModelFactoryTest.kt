package com.germandebustamante.fuelio.core.interop

import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationsResult
import com.germandebustamante.fuelio.core.domain.gasstation.testing.GasStationBOMother
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationUseCase
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationsByLocationUseCase
import com.germandebustamante.fuelio.core.domain.province.testing.ProvinceBOMother
import com.germandebustamante.fuelio.core.domain.province.usecase.GetProvincesUseCase
import com.germandebustamante.fuelio.core.domain.province.usecase.ResolveProvinceByLocationUseCase
import com.germandebustamante.fuelio.core.navigation.action.DefaultNavigator
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.di.presentationPlatformModule
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionController
import com.germandebustamante.fuelio.feature.common.permission.location.LocationPermissionState
import com.germandebustamante.fuelio.feature.detail.di.gasStationDetailModule
import com.germandebustamante.fuelio.feature.list.di.gasStationListModule
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * Coverage for the one piece of interop no library removes: resolving both ViewModels out of the
 * production feature modules, whose Koin definitions take **resolution parameters**
 * (`LocationPermissionController` for the list, `Destination.GasStationDetails` for the detail).
 *
 * State observation and ViewModel lifetime are KMP-NativeCoroutines' and KMP-ObservableViewModel's
 * job and are covered from Swift; what has to be asserted here is that the graph wires up at all.
 *
 * The repositories are mocked but the use cases, Koin modules and `Navigator` are the production
 * ones — `:data` (Ktor/Room) deliberately stays out of the graph.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IosViewModelFactoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private val navigator = DefaultNavigator()

    private val getGasStationsByLocationUseCase: GetGasStationsByLocationUseCase = mock {
        every { invoke(any()) } returns flowOf(
            Result.success(GasStationsResult(GasStationBOMother.gasStationBOList(), isFromCache = false)),
        )
    }

    private val getProvincesUseCase: GetProvincesUseCase = mock {
        every { invoke() } returns flowOf(Result.success(ProvinceBOMother.provinceBOList()))
    }

    private val getGasStationUseCase: GetGasStationUseCase = mock {
        every { invoke(any()) } returns flowOf(GasStationBOMother.gasStationBO())
    }

    private val locationPermissionController: LocationPermissionController = mock {
        everySuspend { requestPermission() } returns LocationPermissionState.DeniedAlways
        everySuspend { checkCurrentStatus() } returns LocationPermissionState.DeniedAlways
        everySuspend { getCurrentLocation() } returns null
        every { openAppSettings() } returns Unit
    }

    private val analyticsTracking: AnalyticsTracking = mock {
        everySuspend { track(any()) } returns Unit
    }

    private val testModule = module {
        single { getGasStationsByLocationUseCase }
        single { getProvincesUseCase }
        single { getGasStationUseCase }
        single { ResolveProvinceByLocationUseCase() }
        single<Navigator> { navigator }
        single<AnalyticsTracking> { analyticsTracking }
        single<LocationPermissionController> { locationPermissionController }
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        startKoin { modules(testModule, gasStationListModule, gasStationDetailModule) }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    /**
     * Asserts the graph wires up — every use case, the navigator and the analytics tracker resolve,
     * and the ViewModel's startup tasks actually run — not the full load pipeline.
     *
     * The station list deliberately is *not* asserted here: the production Koin definition uses the
     * default `Dispatchers.Default`, and `buildGasStationItems` hops onto it with `withContext`, which
     * escapes the test scheduler so `advanceUntilIdle()` cannot wait for it. Loading is covered
     * end-to-end by `GasStationsViewModelTest`, which injects a test dispatcher, and from Swift by
     * `BridgeIntegrationTests`, which polls instead of advancing a scheduler.
     */
    @Test
    fun `case - GIVEN a started Koin graph WHEN the gas stations view model is created THEN its dependencies resolve and startup runs`() = runTest(testDispatcher) {
        val viewModel = IosViewModelFactory.gasStations()

        advanceUntilIdle()

        assertEquals(ProvinceBOMother.provinceBOList(), viewModel.state.value.provinces)
        assertEquals(ProvinceBOMother.provinceBOList().first(), viewModel.state.value.selectedProvince)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `case - GIVEN a station id WHEN the detail view model is created THEN it loads that station`() = runTest(testDispatcher) {
        val expected = GasStationBOMother.gasStationBO()

        val viewModel = IosViewModelFactory.gasStationDetail(expected.id)
        advanceUntilIdle()

        assertEquals(expected.id, viewModel.state.value.gasStation?.id)
    }

    @Test
    fun `case - GIVEN the shared navigator WHEN requested twice THEN the same single instance is returned`() {
        assertSame(navigator, IosViewModelFactory.navigator())
        assertSame(IosViewModelFactory.navigator(), IosViewModelFactory.navigator())
    }

    @Test
    fun `case - GIVEN the isolated navigator WHEN requested THEN it is not the shared single-consumer one`() {
        val isolated = IosViewModelFactory.isolatedNavigator()

        assertNotSame(navigator, isolated)
        assertNotSame(isolated, IosViewModelFactory.isolatedNavigator())
    }

    @Test
    fun `case - GIVEN the iOS platform module WHEN resolving THEN a location permission controller is available`() {
        stopKoin()
        startKoin { modules(presentationPlatformModule) }

        val controller = IosViewModelFactory.getKoin().get<LocationPermissionController>()

        assertNotNull(controller)
    }
}
