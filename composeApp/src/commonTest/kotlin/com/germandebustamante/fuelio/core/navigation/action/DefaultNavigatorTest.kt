package com.germandebustamante.fuelio.core.navigation.action

import app.cash.turbine.test
import com.germandebustamante.fuelio.core.navigation.destination.Destination
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultNavigatorTest {

    private val sut = DefaultNavigator()

    @Test
    fun `navigate - GIVEN a destination WHEN navigate THEN Navigate action is emitted with that destination`() = runTest {
        val destination = Destination.GasStationDetails(gasStationId = "station-1")

        sut.navigationActions.test {
            sut.navigate(destination)

            assertEquals(NavigationAction.Navigate(destination), awaitItem())
        }
    }

    @Test
    fun `navigateUp - WHEN navigateUp THEN Back action is emitted`() = runTest {
        sut.navigationActions.test {
            sut.navigateUp()

            assertEquals(NavigationAction.Back, awaitItem())
        }
    }
}
