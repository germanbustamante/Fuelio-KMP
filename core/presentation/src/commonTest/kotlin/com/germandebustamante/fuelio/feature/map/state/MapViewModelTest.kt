package com.germandebustamante.fuelio.feature.map.state

import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.domain.error.DomainError
import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationsResult
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationsByLocationUseCase
import com.germandebustamante.fuelio.core.domain.preferences.model.UserPreferencesBO
import com.germandebustamante.fuelio.core.domain.preferences.usecase.ObserveUserPreferencesUseCase
import com.germandebustamante.fuelio.core.fake.fakeGasStations
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.feature.map.analytics.MapMarkerSelected
import com.germandebustamante.fuelio.feature.map.analytics.MapScreenViewed
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
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
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val observeUserPreferencesUseCase: ObserveUserPreferencesUseCase = mock {
        every { invoke() } returns flowOf(UserPreferencesBO(savedProvinceId = "28"))
    }

    private val getGasStationsByLocationUseCase: GetGasStationsByLocationUseCase = mock {
        every { invoke(any()) } returns flowOf(Result.success(GasStationsResult(fakeGasStations, isFromCache = false)))
    }

    private val navigator: Navigator = mock {
        everySuspend { navigate(any()) } returns Unit
        everySuspend { navigateUp() } returns Unit
    }

    private val analyticsManager: AnalyticsTracking = mock {
        everySuspend { track(any()) } returns Unit
    }

    private lateinit var sut: MapViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init - GIVEN the screen opens THEN the screen view is tracked`() = runTest {
        createSut()
        advanceUntilIdle()

        verifySuspend { analyticsManager.track(MapScreenViewed) }
    }

    @Test
    fun `observeMarkers - GIVEN a saved province THEN the stations are mapped into markers`() = runTest {
        createSut()
        advanceUntilIdle()

        val content = assertIs<MapContentState.Success>(sut.state.value.contentState)
        assertEquals(fakeGasStations.map { it.id }, content.markers.map { it.gasStationId })
    }

    @Test
    fun `observeMarkers - GIVEN no saved province THEN the content state is empty`() = runTest {
        every { observeUserPreferencesUseCase() } returns flowOf(UserPreferencesBO(savedProvinceId = null))

        createSut()
        advanceUntilIdle()

        assertEquals(MapContentState.Empty, sut.state.value.contentState)
    }

    @Test
    fun `observeMarkers - GIVEN the fetch fails THEN the content state is an error`() = runTest {
        every { getGasStationsByLocationUseCase(any()) } returns flowOf(Result.failure(DomainError.NetworkUnavailable))

        createSut()
        advanceUntilIdle()

        assertIs<MapContentState.Error>(sut.state.value.contentState)
    }

    @Test
    fun `onMarkerSelected - GIVEN a marker WHEN tapped THEN it is tracked and navigated to`() = runTest {
        createSut()
        advanceUntilIdle()

        sut.onMarkerSelected("7153")
        advanceUntilIdle()

        verifySuspend { analyticsManager.track(MapMarkerSelected("7153")) }
        verifySuspend { navigator.navigate(Destination.GasStationDetails("7153")) }
    }

    @Test
    fun `onBackClick - GIVEN the screen WHEN back is tapped THEN navigation goes through the navigator`() = runTest {
        createSut()
        advanceUntilIdle()

        sut.onBackClick()
        advanceUntilIdle()

        verifySuspend { navigator.navigateUp() }
    }

    private fun createSut(initialState: MapUIState = MapUIState()) {
        sut = MapViewModel(
            observeUserPreferencesUseCase = observeUserPreferencesUseCase,
            getGasStationsByLocationUseCase = getGasStationsByLocationUseCase,
            navigator = navigator,
            analyticsManager = analyticsManager,
            initialState = initialState,
        )
    }
}
