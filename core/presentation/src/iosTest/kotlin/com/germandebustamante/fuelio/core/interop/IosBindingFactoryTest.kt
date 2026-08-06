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
import com.germandebustamante.fuelio.core.navigation.action.NavigationAction
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.destination.Destination
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration coverage for the DI half of the bridge: the real feature Koin modules resolve both
 * ViewModels through [IosBindingFactory] with their resolution parameters supplied on the Kotlin
 * side, and the bindings emit state to Swift-shaped callbacks.
 *
 * The repositories are mocked but the use cases, Koin modules and `Navigator` are the production
 * ones — `:data` (Ktor/Room) deliberately stays out of the graph.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IosBindingFactoryTest {

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

    @Test
    fun `case - GIVEN a started Koin graph WHEN the gas stations binding is created THEN its state reaches the callback`() = runTest(testDispatcher) {
        val binding = IosBindingFactory.createGasStationsBinding()
        val received = mutableListOf<Int>()

        val subscription = binding.observeState { received += it.gasStations.size }
        advanceUntilIdle()

        assertTrue(received.isNotEmpty(), "the binding should deliver at least the current state")
        assertEquals(binding.currentState.gasStations.size, received.last())
        subscription.cancel()
        binding.close()
    }

    @Test
    fun `case - GIVEN a station id WHEN the detail binding is created THEN it loads that station`() = runTest(testDispatcher) {
        val expected = GasStationBOMother.gasStationBO()

        val binding = IosBindingFactory.createGasStationDetailBinding(expected.id)
        advanceUntilIdle()

        assertNotNull(binding.currentState.gasStation)
        assertEquals(expected.id, binding.currentState.gasStation?.id)
        binding.close()
    }

    @Test
    fun `case - GIVEN the navigation binding WHEN the navigator navigates THEN the action reaches the callback`() = runTest(testDispatcher) {
        val binding = IosBindingFactory.createNavigationBinding()
        val received = mutableListOf<NavigationAction>()
        binding.observeNavigation { received += it }
        advanceUntilIdle()

        navigator.navigate(Destination.GasStationDetails("7153"))
        advanceUntilIdle()

        assertEquals(1, received.size)
        val action = assertIs<NavigationAction.Navigate>(received.single())
        assertEquals(Destination.GasStationDetails("7153"), action.destination)
        binding.close()
    }

    @Test
    fun `case - GIVEN the iOS platform module WHEN resolving THEN a location permission controller is available`() {
        stopKoin()
        startKoin { modules(presentationPlatformModule) }

        val controller = IosBindingFactory.getKoin().get<LocationPermissionController>()

        assertNotNull(controller)
    }
}
