# 0002 — `:core:presentation` module with no Compose dependency

- **Status:** Accepted
- **Date:** 2026-08-01
- **Depends on:** [0001](0001-kmp-with-native-ui-instead-of-compose-multiplatform.md)

## Context

After deciding to migrate iOS to native SwiftUI ([0001](0001-kmp-with-native-ui-instead-of-compose-multiplatform.md)),
a Kotlin/Native framework that Xcode can import directly is needed. Before this change, `ViewModel`s,
`UIState`s, `Navigator`/`Destination`, and the analytics `Trace` types lived in
`composeApp/src/commonMain` alongside the entire Compose tree (`designsystem/`, `feature/*/ui/`,
`core/ui/theme/`) — by convenience, not by design: that code never imported Compose, it simply shared a
module with code that did.

That was viable while `composeApp` was the only consumer of that code on both platforms. It stops
being viable the moment iOS needs to consume the presentation logic **without** pulling Jetpack
Compose / Compose Multiplatform Resources / Navigation3 into a framework that Xcode is going to link.

## Decision

Extract a new Gradle module, `:core:presentation` (`:core:*` prefix, matching `:core:domain` and
`:core:analytics`), containing all presentation logic that does **not** depend on Compose:
`GasStationsViewModel`, `GasStationDetailViewModel`, their `UIState`s, `Navigator`/`NavigationAction`/
`Destination`, the Koin modules (`KoinInit.kt`, `*Module.kt`), `LocationPermissionController` (interface
+ Android/iOS `actual`s), the analytics `Trace` types, and — added in the same change — the shared
design tokens (`core/designtokens/`).

**Module invariant:** zero imports of `androidx.compose.*`, `org.jetbrains.compose.*`, or
`androidx.navigation3.*`. Verified with:

```bash
grep -r "androidx.compose\|org.jetbrains.compose\|navigation3" core/presentation/src
```

This forced two specific decoupling changes:

1. **`Destination` implemented `NavKey`** (`androidx.navigation3.runtime`), the only Navigation3
   dependency in the otherwise Compose-free code. `Destination` drops the `NavKey` inheritance; the
   coupling is resolved in `:androidApp` with a wrapper (`DestinationNavKey`) that is the only place in
   the app where `Destination` touches Navigation3.
2. **`GasStationItemVO`/`GasStationsUIState` carried `@Immutable`** (`androidx.compose.runtime`). The
   annotation is dropped; stability for the Compose compiler is instead declared in
   `androidApp/compose_stability.conf` (`composeCompiler { stabilityConfigurationFiles.add(...) }`),
   which has the same effect without needing the annotation on the type itself.

`:androidApp` (formerly `composeApp`) becomes a plain Android module (`com.android.application`, no
Kotlin Multiplatform plugin) that depends on `:core:presentation`. The iOS framework is declared in
`:core:presentation` (`baseName = "CorePresentation"`, `export(projects.core.analytics)` so the native
analytics bridges stay visible from Swift).

## Alternatives considered

**Keep `Destination : NavKey`, add `navigation3-runtime` to `:core:presentation`.** Zero code changes
in `Destination.kt`, but the framework exported to iOS would end up including `compose-runtime` on the
classpath — exactly what 0001 set out to avoid. Rejected.

## Consequences

- `:core:presentation` is now the module tested with `iosSimulatorArm64Test` — the ~40 ViewModel tests
  live there, not in `:androidApp`.
- Any new presentation-layer ViewModel/UIState/use case gets added to `:core:presentation` by default;
  only code that literally draws UI (Composables) goes in `:androidApp`.
- The dependency graph is now: `:androidApp → :core:presentation → :data → :core:domain`, with
  `:core:analytics` as a separate branch consumed via `api` from `:core:presentation`.
