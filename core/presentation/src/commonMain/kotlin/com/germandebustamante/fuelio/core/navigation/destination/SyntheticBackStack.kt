package com.germandebustamante.fuelio.core.navigation.destination

/**
 * A screen's parent in the app's conceptual hierarchy — not "where the user came from," but "which
 * screen should a respecting `back` land on." It exists solely to rebuild a navigation stack when an
 * external link drops the user directly onto an interior screen, so `back` leaves them on the list
 * instead of exiting the app.
 *
 * Deliberately an extension property with an exhaustive `when`, not a `DeepLinkDestination:
 * Destination` sub-interface: SKIE turns **every direct subtype** of a sealed interface into a case
 * of the generated Swift enum, so that sub-interface added a
 * `case deepLinkDestination(DeepLinkDestination)` which, being checked with `as?` before the concrete
 * types, left `case gasStationDetails` as unreachable dead code and forced a runtime downcast in
 * Swift — exactly the silent failure mode the sealed→enum mapping exists to eliminate (see
 * `Support/KotlinSealed.swift` and CLAUDE.md, rule 5). This `when` has no `else`, so a new screen is
 * still a compile error here.
 *
 * `internal` on purpose: neither `:androidApp` nor Swift needs it directly (both only call
 * [buildSyntheticBackStack]), so it never shows up in the generated Objective-C header.
 */
internal val Destination.parent: Destination?
    get() = when (this) {
        is Destination.GasStations -> null
        is Destination.GasStationDetails -> Destination.GasStations
        is Destination.Settings -> Destination.GasStations
        is Destination.Favorites -> Destination.GasStations
    }

/**
 * The synthetic navigation stack an external link should land on: every ancestor of [target], from
 * the root down, followed by [target] itself. For the root, returns just the root.
 */
fun buildSyntheticBackStack(target: Destination): List<Destination> = buildList {
    add(target)
    var node: Destination? = target.parent
    while (node != null) {
        add(0, node)
        node = node.parent
    }
}
