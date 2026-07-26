package com.germandebustamante.fuelio.feature.detail.state

import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.domain.gasstation.testing.GasStationBOMother
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationUseCase
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import com.germandebustamante.fuelio.feature.detail.analytics.GasStationDetailScreenViewed
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
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

    private lateinit var sut : GasStationDetailViewModel

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
        //GIVEN
        val screenViewedTraceSlot = Capture.slot<GasStationDetailScreenViewed>()
        everySuspend { analyticsManager.track(capture(screenViewedTraceSlot)) } returns Unit

        //WHEN
        sut = GasStationDetailViewModel(route, getGasStation, navigator, analyticsManager)
        advanceUntilIdle()

        //THEN
        assertEquals(GasStationDetailScreenViewed.SCREEN_NAME, screenViewedTraceSlot.get().screenName)
        assertEquals(
            GAS_STATION_ID,
            screenViewedTraceSlot.get().params?.get(GasStationDetailScreenViewed.PARAM_GAS_STATION_ID)
        )
    }

    companion object {
        private const val GAS_STATION_ID: String = "station-1"
    }
}
