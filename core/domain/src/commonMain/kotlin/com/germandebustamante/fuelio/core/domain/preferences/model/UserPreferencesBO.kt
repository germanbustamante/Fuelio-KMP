package com.germandebustamante.fuelio.core.domain.preferences.model

/**
 * The choices that survive a restart.
 *
 * Every field has a default, so "nothing stored yet" is a valid, fully-formed value rather than a
 * nullable state every consumer has to handle. [savedProvinceId] is the one genuine null: it means
 * the user has never picked a province, which is what keeps the geolocation path alive on a first
 * launch.
 */
data class UserPreferencesBO(
    val defaultFuelType: FuelType = FuelType.GASOLINE_95,
    val savedProvinceId: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
