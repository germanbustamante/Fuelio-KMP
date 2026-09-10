package com.germandebustamante.fuelio.core.domain.preferences.model

/**
 * The fuel a user cares about.
 *
 * A domain-level enum rather than a reuse of `FuelFilter`, which is a presentation-layer sealed
 * interface in `:core:presentation` that `:core:domain` cannot depend on. Being an enum also matters
 * on the bridge: SKIE maps Kotlin enums straight onto Swift enums, without the exported-subtype
 * hazard that sealed hierarchies carry (see ADR 0004).
 */
enum class FuelType {
    GASOLINE_95,
    GASOLINE_98,
    DIESEL,
    DIESEL_PREMIUM,
}
