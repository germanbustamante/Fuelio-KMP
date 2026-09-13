package com.germandebustamante.fuelio.core.domain.installation.repository

/**
 * A random, per-install anonymous id used as the analytics `distinct_id` before/regardless of any
 * real user identity. Deliberately separate from [com.germandebustamante.fuelio.core.domain.preferences.repository.UserPreferencesRepository]:
 * that repository models the user's own choices (`observe()` is a `Flow` other screens react to),
 * while this id is device state nobody chooses and nothing should re-emit on.
 */
interface InstallationIdRepository {

    suspend fun getOrCreate(): String
}
