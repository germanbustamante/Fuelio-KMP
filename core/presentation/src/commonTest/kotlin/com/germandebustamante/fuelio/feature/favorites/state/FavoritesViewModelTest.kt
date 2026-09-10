package com.germandebustamante.fuelio.feature.favorites.state

import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.domain.gasstation.model.FavoriteStationsResult
import com.germandebustamante.fuelio.core.domain.gasstation.model.GasStationBO
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.ObserveFavoriteStationsUseCase
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.ToggleFavoriteStationUseCase
import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.domain.preferences.model.UserPreferencesBO
import com.germandebustamante.fuelio.core.domain.preferences.usecase.ObserveUserPreferencesUseCase
import com.germandebustamante.fuelio.core.fake.fakeGasStations
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.feature.favorites.analytics.FavoritesScreenViewed
import com.germandebustamante.fuelio.feature.list.analytics.GasStationSelected
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
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
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    // Prices chosen so the cheapest station flips depending on the fuel: the whole point of the
    // screen is comparing favourites by *the user's* default fuel, not by gasoline 95.
    private val cheapOn95 = fakeGasStations[0].copy(
        id = "cheap-95",
        gasolinePrice95 = 1.400,
        dieselPrice = 1.900,
    )
    private val cheapOnDiesel = fakeGasStations[1].copy(
        id = "cheap-diesel",
        gasolinePrice95 = 1.600,
        dieselPrice = 1.700,
    )

    private val observeFavoriteStationsUseCase: ObserveFavoriteStationsUseCase = mock {
        every { invoke() } returns flowOf(FavoriteStationsResult(emptyList(), totalFavoriteCount = 0))
    }

    private val observeUserPreferencesUseCase: ObserveUserPreferencesUseCase = mock {
        every { invoke() } returns flowOf(UserPreferencesBO())
    }

    private val toggleFavoriteStationUseCase: ToggleFavoriteStationUseCase = mock {
        everySuspend { invoke(any()) } returns Unit
    }

    private val navigator: Navigator = mock {
        everySuspend { navigate(any()) } returns Unit
        everySuspend { navigateUp() } returns Unit
    }

    private val analyticsManager: AnalyticsTracking = mock {
        everySuspend { track(any()) } returns Unit
    }

    private lateinit var sut: FavoritesViewModel

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

        verifySuspend { analyticsManager.track(FavoritesScreenViewed) }
    }

    @Test
    fun `observeFavorites - GIVEN no favorites THEN the content state is empty`() = runTest {
        createSut()
        advanceUntilIdle()

        assertEquals(FavoritesContentState.Empty, sut.state.value.contentState)
        assertFalse(sut.state.value.isLoading)
    }

    @Test
    fun `observeFavorites - GIVEN favorites THEN they are sorted by the default fuel price`() = runTest {
        givenFavorites(cheapOnDiesel, cheapOn95)
        givenPreferences(FuelType.GASOLINE_95)

        createSut()
        advanceUntilIdle()

        assertEquals(listOf("cheap-95", "cheap-diesel"), sut.state.value.stations.map { it.station.id })
    }

    @Test
    fun `observeFavorites - GIVEN the default fuel changes THEN the order follows it`() = runTest {
        // Re-sorting on a preference change is why the two flows are combined rather than read once.
        val preferences = MutableStateFlow(UserPreferencesBO(defaultFuelType = FuelType.GASOLINE_95))
        every { observeUserPreferencesUseCase() } returns preferences
        givenFavorites(cheapOnDiesel, cheapOn95)
        createSut()
        advanceUntilIdle()
        assertEquals(listOf("cheap-95", "cheap-diesel"), sut.state.value.stations.map { it.station.id })

        preferences.value = UserPreferencesBO(defaultFuelType = FuelType.DIESEL)
        advanceUntilIdle()

        assertEquals(listOf("cheap-diesel", "cheap-95"), sut.state.value.stations.map { it.station.id })
    }

    @Test
    fun `observeFavorites - GIVEN favorites THEN only the first station at the cheapest price is marked`() = runTest {
        givenFavorites(cheapOn95, cheapOn95.copy(id = "tie"))
        givenPreferences(FuelType.GASOLINE_95)

        createSut()
        advanceUntilIdle()

        assertEquals(1, sut.state.value.stations.count { it.isCheapest })
        assertTrue(sut.state.value.stations.first().isCheapest)
    }

    @Test
    fun `observeFavorites - GIVEN a station without the default fuel price THEN it is sorted last`() = runTest {
        givenFavorites(cheapOn95.copy(id = "no-price", gasolinePrice95 = null), cheapOnDiesel)
        givenPreferences(FuelType.GASOLINE_95)

        createSut()
        advanceUntilIdle()

        assertEquals(listOf("cheap-diesel", "no-price"), sut.state.value.stations.map { it.station.id })
    }

    @Test
    fun `observeFavorites - GIVEN favorites whose station is not cached THEN the unresolved count is surfaced`() = runTest {
        // A shorter list is business state, not an error: the screen tells the user rather than
        // looking like it lost the favourites.
        every { observeFavoriteStationsUseCase() } returns
            flowOf(FavoriteStationsResult(listOf(cheapOn95), totalFavoriteCount = 3))

        createSut()
        advanceUntilIdle()

        val content = assertIs<FavoritesContentState.Success>(sut.state.value.contentState)
        assertEquals(2, content.unresolvedCount)
    }

    @Test
    fun `onToggleFavorite - GIVEN a station WHEN untoggled THEN the repository is the one updated`() = runTest {
        createSut()
        advanceUntilIdle()

        sut.onToggleFavorite("cheap-95")
        advanceUntilIdle()

        verifySuspend { toggleFavoriteStationUseCase("cheap-95") }
    }

    @Test
    fun `onItemClick - GIVEN a station WHEN tapped THEN it is tracked and navigated to`() = runTest {
        createSut()
        advanceUntilIdle()

        sut.onItemClick("7153")
        advanceUntilIdle()

        verifySuspend { analyticsManager.track(GasStationSelected("7153")) }
        verifySuspend { navigator.navigate(Destination.GasStationDetails("7153")) }
    }

    @Test
    fun `onBackTapped - GIVEN the screen WHEN back is tapped THEN navigation goes through the navigator`() = runTest {
        createSut()
        advanceUntilIdle()

        sut.onBackTapped()
        advanceUntilIdle()

        verifySuspend { navigator.navigateUp() }
    }

    private fun givenFavorites(vararg stations: GasStationBO) {
        every { observeFavoriteStationsUseCase() } returns
            flowOf(FavoriteStationsResult(stations.toList(), totalFavoriteCount = stations.size))
    }

    private fun givenPreferences(fuelType: FuelType) {
        every { observeUserPreferencesUseCase() } returns flowOf(UserPreferencesBO(defaultFuelType = fuelType))
    }

    private fun createSut(initialState: FavoritesUIState = FavoritesUIState()) {
        sut = FavoritesViewModel(
            observeFavoriteStationsUseCase = observeFavoriteStationsUseCase,
            observeUserPreferencesUseCase = observeUserPreferencesUseCase,
            toggleFavoriteStationUseCase = toggleFavoriteStationUseCase,
            navigator = navigator,
            analyticsManager = analyticsManager,
            defaultDispatcher = testDispatcher,
            initialState = initialState,
        )
    }
}
