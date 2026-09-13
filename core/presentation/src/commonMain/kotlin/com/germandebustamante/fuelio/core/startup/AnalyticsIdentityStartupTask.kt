package com.germandebustamante.fuelio.core.startup

import com.germandebustamante.fuelio.core.analytics.AnalyticsTracking
import com.germandebustamante.fuelio.core.build.BuildEnvironment
import com.germandebustamante.fuelio.core.domain.installation.repository.InstallationIdRepository
import com.germandebustamante.fuelio.core.domain.preferences.usecase.ObserveUserPreferencesUseCase
import kotlinx.coroutines.flow.first

/**
 * Identifies this install to every registered analytics tracker exactly once per launch.
 *
 * The distinct id is a per-device random uuid, not a real user id — there is no login in this app —
 * and the super-properties are read once at launch rather than observed, since re-identifying on
 * every preference change would be a much bigger behavior change than this task is meant to be.
 */
class AnalyticsIdentityStartupTask(
    private val installationIdRepository: InstallationIdRepository,
    private val observeUserPreferencesUseCase: ObserveUserPreferencesUseCase,
    private val analyticsManager: AnalyticsTracking,
    private val buildEnvironment: BuildEnvironment,
) : StartupTask {

    override suspend fun invoke() {
        val installationId = installationIdRepository.getOrCreate()
        val defaultFuelType = observeUserPreferencesUseCase().first().defaultFuelType

        analyticsManager.identify(
            distinctId = installationId,
            properties = mapOf(
                PROPERTY_PLATFORM to buildEnvironment.platform,
                PROPERTY_DEFAULT_FUEL_TYPE to defaultFuelType.name,
            ),
        )
    }

    private companion object {
        const val PROPERTY_PLATFORM = "platform"
        const val PROPERTY_DEFAULT_FUEL_TYPE = "default_fuel_type"
    }
}
