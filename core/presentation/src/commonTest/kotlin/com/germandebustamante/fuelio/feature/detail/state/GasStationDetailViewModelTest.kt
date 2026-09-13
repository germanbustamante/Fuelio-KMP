package com.germandebustamante.fuelio.feature.detail.state

import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.domain.gasstation.testing.GasStationBOMother
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationUseCase
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.ObserveFavoriteStationIdsUseCase
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.ToggleFavoriteStationUseCase
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.feature.detail.analytics.DirectionsRequested
import com.germandebustamante.fuelio.feature.detail.analytics.GasStationDetailScreenViewed
import com.germandebustamante.fuelio.feature.list.analytics.FavoriteToggled
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GasStationDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val route = Destination.GasStationDetails(gasStationId = GAS_STATION_ID)

    private val getGasStation: GetGasStationUseCase = mock {
        every { invoke(route.gasStationId) } returns flowOf(GasStationBOMother.gasStationBO(id = route.gasStationId))
    }

    private val navigator: Navigator = mock()

    private val analyticsManager: AnalyticsTracking = mock {
        everySuspend { track(any()) } returns Unit
    }

    private val observeFavoriteStationIdsUseCase: ObserveFavoriteStationIdsUseCase = mock {
        every { invoke() } returns flowOf(emptySet())
    }

    private val toggleFavoriteStationUseCase: ToggleFavoriteStationUseCase = mock {
        everySuspend { invoke(any()) } returns Unit
    }

    private lateinit var sut: GasStationDetailViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init - WHEN the ViewModel is created THEN a gas_station_detail screen trace is tracked with the station id`() = runTest {
        // GIVEN
        val screenViewedTraceSlot = Capture.slot<GasStationDetailScreenViewed>()
        everySuspend { analyticsManager.track(capture(screenViewedTraceSlot)) } returns Unit

        // WHEN
        createSut()
        advanceUntilIdle()

        // THEN
        assertEquals(GasStationDetailScreenViewed.SCREEN_NAME, screenViewedTraceSlot.get().screenName)
        assertEquals(
            GAS_STATION_ID,
            screenViewedTraceSlot.get().params?.get(GasStationDetailScreenViewed.PARAM_GAS_STATION_ID),
        )
    }

    @Test
    fun `onDirectionsTapped - WHEN called THEN a directions_requested trace is tracked with the station id`() = runTest {
        // GIVEN
        val directionsTraceSlot = Capture.slot<DirectionsRequested>()
        everySuspend { analyticsManager.track(capture(directionsTraceSlot)) } returns Unit
        createSut()
        advanceUntilIdle()

        // WHEN
        sut.onDirectionsTapped()
        advanceUntilIdle()

        // THEN
        assertEquals(GAS_STATION_ID, directionsTraceSlot.get().gasStationId)
    }

    @Test
    fun `init - GIVEN the station is already a favorite THEN isFavorite is true on a cold start`() = runTest {
        every { observeFavoriteStationIdsUseCase() } returns flowOf(setOf(GAS_STATION_ID))

        createSut()
        advanceUntilIdle()

        assertTrue(sut.state.value.isFavorite)
    }

    @Test
    fun `onToggleFavorite - GIVEN the station is not a favorite WHEN toggled THEN it is delegated to the repository and tracked as added`() = runTest {
        createSut()
        advanceUntilIdle()

        sut.onToggleFavorite()
        advanceUntilIdle()

        verifySuspend { toggleFavoriteStationUseCase(GAS_STATION_ID) }
        verifySuspend { analyticsManager.track(FavoriteToggled(GAS_STATION_ID, isFavorite = true)) }
    }

    @Test
    fun `onToggleFavorite - GIVEN the station is already a favorite WHEN toggled THEN it is tracked as removed`() = runTest {
        every { observeFavoriteStationIdsUseCase() } returns flowOf(setOf(GAS_STATION_ID))
        createSut()
        advanceUntilIdle()

        sut.onToggleFavorite()
        advanceUntilIdle()

        verifySuspend { analyticsManager.track(FavoriteToggled(GAS_STATION_ID, isFavorite = false)) }
    }

    @Test
    fun `onToggleFavorite - WHEN toggled THEN the state does not change until the repository emits`() = runTest {
        val favorites = MutableStateFlow(emptySet<String>())
        every { observeFavoriteStationIdsUseCase() } returns favorites
        createSut()
        advanceUntilIdle()

        sut.onToggleFavorite()
        advanceUntilIdle()

        assertFalse(sut.state.value.isFavorite)

        favorites.value = setOf(GAS_STATION_ID)
        advanceUntilIdle()
        assertTrue(sut.state.value.isFavorite)
    }

    private fun createSut() {
        sut = GasStationDetailViewModel(
            route = route,
            getGasStation = getGasStation,
            navigator = navigator,
            analyticsManager = analyticsManager,
            observeFavoriteStationIdsUseCase = observeFavoriteStationIdsUseCase,
            toggleFavoriteStationUseCase = toggleFavoriteStationUseCase,
        )
    }

    companion object {
        private const val GAS_STATION_ID: String = "station-1"
    }
}
