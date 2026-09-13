package com.germandebustamante.fuelio.core.startup

import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.build.BuildEnvironment
import com.germandebustamante.fuelio.core.domain.installation.repository.InstallationIdRepository
import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType
import com.germandebustamante.fuelio.core.domain.preferences.model.UserPreferencesBO
import com.germandebustamante.fuelio.core.domain.preferences.usecase.ObserveUserPreferencesUseCase
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class AnalyticsIdentityStartupTaskTest {

    private val installationIdRepository: InstallationIdRepository = mock {
        everySuspend { getOrCreate() } returns INSTALLATION_ID
    }

    private val observeUserPreferencesUseCase: ObserveUserPreferencesUseCase = mock {
        every { invoke() } returns flowOf(UserPreferencesBO(defaultFuelType = FuelType.DIESEL))
    }

    private val analyticsManager: AnalyticsTracking = mock {
        everySuspend { identify(any(), any()) } returns Unit
    }

    @Test
    fun `invoke - WHEN run THEN the analytics manager is identified with the installation id`() = runTest {
        createSut()()

        verifySuspend { analyticsManager.identify(INSTALLATION_ID, any()) }
    }

    @Test
    fun `invoke - WHEN run THEN the platform and default fuel are attached as super-properties`() = runTest {
        createSut(platform = BuildEnvironment.PLATFORM_IOS)()

        verifySuspend {
            analyticsManager.identify(
                INSTALLATION_ID,
                mapOf("platform" to BuildEnvironment.PLATFORM_IOS, "default_fuel_type" to FuelType.DIESEL.name),
            )
        }
    }

    private fun createSut(platform: String = BuildEnvironment.PLATFORM_ANDROID) = AnalyticsIdentityStartupTask(
        installationIdRepository = installationIdRepository,
        observeUserPreferencesUseCase = observeUserPreferencesUseCase,
        analyticsManager = analyticsManager,
        buildEnvironment = BuildEnvironment(isDebug = false, platform = platform),
    )

    private companion object {
        const val INSTALLATION_ID = "installation-1"
    }
}
