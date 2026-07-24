package com.germandebustamante.fuelio.feature.detail.state

import com.germandebustamante.fuelio.core.analytics.AnalyticsManager
import com.germandebustamante.fuelio.core.analytics.AnalyticsProviderType
import com.germandebustamante.fuelio.core.analytics.Trace
import com.germandebustamante.fuelio.core.analytics.Trackable
import com.germandebustamante.fuelio.core.domain.gasstation.testing.GasStationBOMother
import com.germandebustamante.fuelio.core.domain.gasstation.usecase.GetGasStationUseCase
import com.germandebustamante.fuelio.core.navigation.action.Navigator
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals

class GasStationDetailViewModelTest {

    private val route = Destination.GasStationDetails(gasStationId = "station-1")

    private val getGasStation: GetGasStationUseCase = mock {
        every { invoke(route.gasStationId) } returns flowOf(GasStationBOMother.gasStationBO(id = route.gasStationId))
    }

    private val navigator: Navigator = mock()

    private val trackedTraces = mutableListOf<Trace>()
    private val fakeTracker = object : Trackable {
        override val type = AnalyticsProviderType.FIREBASE
        override fun track(trace: Trace) {
            trackedTraces.add(trace)
        }
    }
    private val analyticsManager = AnalyticsManager(listOf(fakeTracker))

    @Test
    fun `init - WHEN the ViewModel is created THEN a gas_station_detail screen trace is tracked with the station id`() {
        GasStationDetailViewModel(route, getGasStation, navigator, analyticsManager)

        val expectedTrace: Trace = Trace.Screen(
            screenName = "gas_station_detail",
            targets = listOf(AnalyticsProviderType.FIREBASE),
            params = mapOf("gas_station_id" to route.gasStationId),
        )
        assertEquals(listOf(expectedTrace), trackedTraces)
    }
}
