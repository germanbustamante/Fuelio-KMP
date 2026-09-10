package com.germandebustamante.fuelio.core.navigation.deeplink

import com.germandebustamante.fuelio.core.navigation.destination.Destination

internal object DeepLinkRoutes {
    const val SCHEME_FUELIO = "fuelio"
    const val RESOURCE_STATION = "station"
    const val ACTION_DETAIL = "detail"
}

/**
 * Flat path-segment view of a URI, mirroring how a web URL's path is just segments after the
 * authority — so the same segment-matching logic below can be reused as-is once an `https://`
 * scheme is added later, without depending on scheme-specific host/authority semantics.
 * [host] is a convenience accessor over the first segment (the resource name), for readability
 * at call sites that only care about "what resource is this" (e.g. `parsed.host == RESOURCE_STATION`).
 */
internal data class ParsedUri(val scheme: String?, val pathSegments: List<String>) {
    val host: String? get() = pathSegments.firstOrNull()
}

internal fun parseUri(uri: String): ParsedUri? {
    val schemeSplit = uri.split("://", limit = 2)
    if (schemeSplit.size != 2) return null
    val scheme = schemeSplit[0].takeIf { it.isNotBlank() } ?: return null

    val afterScheme = schemeSplit[1].substringBefore('?').substringBefore('#')
    val pathSegments = afterScheme.split('/').filter { it.isNotBlank() }

    return ParsedUri(scheme = scheme, pathSegments = pathSegments)
}

/**
 * Matches a raw external URI against the app's supported deep-link routes.
 * Returns null for any malformed or unsupported URI — callers must treat that as a no-op,
 * never a crash.
 *
 * Routes are shaped like a web resource path, `station/{gasStationId}/{action}`, so a station's
 * id is resolved once and every action (`detail` today, e.g. a future `map`) branches off it.
 * Adding a new action is one more `when` branch, not a redesign of this function.
 *
 * Returns a plain `Destination`, not a bespoke "enlazable" type — see `SyntheticBackStack.kt`: any
 * extra subtype of `Destination` breaks the enum SKIE generates for Swift.
 */
fun parseDeepLink(uri: String): Destination? {
    val parsed = parseUri(uri) ?: return null
    if (parsed.scheme != DeepLinkRoutes.SCHEME_FUELIO) return null
    if (parsed.host != DeepLinkRoutes.RESOURCE_STATION) return null

    val gasStationId = parsed.pathSegments.getOrNull(1) ?: return null
    if (parsed.pathSegments.size != 3) return null

    return when (parsed.pathSegments.getOrNull(2)) {
        DeepLinkRoutes.ACTION_DETAIL -> Destination.GasStationDetails(gasStationId)
        else -> null
    }
}
