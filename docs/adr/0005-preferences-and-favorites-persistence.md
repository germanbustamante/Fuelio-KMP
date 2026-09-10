# 0005 — Preferences in DataStore, favourites in their own Room table

- **Status:** Accepted
- **Date:** 2026-09-10
- **Depends on:** [0002](0002-core-presentation-module-without-compose.md)

## Context

Two pieces of state survived only as long as the process:

- **The user's context.** The province was re-resolved from geolocation on every cold start, the fuel
  filter always reset to `FuelFilter.Gasoline95`, and the theme could only follow the system. The
  only key-value storage in the repo was a stray `SharedPreferences` inside
  `AndroidLocationPermissionController`, with no iOS counterpart.
- **Favourites.** `GasStationsUIState.favorites: Set<String>` was ViewModel state, so every star went
  dark when the process died.

Both need to work identically on Android and iOS, and both belong below the presentation layer.

## Decision

### Preferences → `androidx.datastore:datastore-preferences-core`

`UserPreferencesRepository` in `:core:domain` exposes `observe(): Flow<UserPreferencesBO>` plus one
suspend setter per field; `:data` implements it over a DataStore built with
`PreferenceDataStoreFactory.createWithPath { producePath().toPath() }`.

Only the path is platform-specific — `expect fun preferencesPath(contextProvider: ContextProvider)`,
Android returning `filesDir/fuelio.preferences_pb` and iOS `NSDocumentDirectory` + the same file
name. That reuses the existing `ContextProvider` seam rather than adding another platform module,
exactly as `FuelioDatabase` already does. The file name **must** end in `.preferences_pb`; the
factory rejects anything else.

The `-core` artifact is the pure-Kotlin variant, which is the one that resolves for
`iosArm64`/`iosSimulatorArm64`.

`FuelType` and `ThemeMode` are plain enums, not sealed hierarchies: SKIE maps a Kotlin enum onto a
Swift enum with none of the subtype caveats that apply to `Destination` (see
[ADR 0004](0004-kotlin-swift-interop-libraries.md)), and the domain should not inherit the shape of
the presentation layer's `FuelFilter`.

### Favourites → a dedicated `favorite_stations` table

A `isFavorite` column on `GasStationEntity` does not work here.
`GasStationDAO.replaceGasStationsByProvince` is a `@Transaction { DELETE WHERE provinceId; INSERT }`
that runs on **every** network refresh, including pull-to-refresh — the column would be wiped each
time. A separate table survives the refresh, spans provinces, and its 1→2 migration is purely
additive.

The consequence is that a favourite is only *renderable* while its station is cached.
`FavoriteStationDAO.observeFavoriteStations()` is an `INNER JOIN`, so it silently drops the rest —
which is why the repository returns `FavoriteStationsResult(stations, totalFavoriteCount)` and the
screen shows the gap as a banner. Modelling it explicitly is the point: a shorter list is expected
business state, not an error, and not something to hide.

## Alternatives considered

- **`multiplatform-settings`** — thinner and synchronous, wrapping `SharedPreferences`/
  `NSUserDefaults`. Rejected because the reactive API is an add-on over a blocking store, whereas
  DataStore is `Flow`-first end to end, which is what both the settings screen and the list screen's
  re-sorting actually consume. It would also have kept `NSUserDefaults` semantics on iOS, where the
  data is neither transactional nor corruption-safe.
- **A single Room table for preferences too** — one row, one column per setting. Rejected: it drags
  a full SQLite transaction into the startup path for four scalar values, and every new preference
  becomes a schema migration.
- **`isFavorite` column on `GasStationEntity`** — see above; it is deleted on every refresh.
- **Keeping favourites in memory and syncing on exit** — loses data on process death, which is the
  common case on Android.

## Consequences

- Two preference reads now sit on the launch path. Both are `Flow`-based and the ViewModel guards
  them (`hasRestoredSavedProvince`, `hasUserSelectedFuel`) so a slow disk read can never overwrite a
  choice the user already made — a race that iOS's `BridgeIntegrationTests` caught in practice.
- The settings screen's theme choice has to be resolved *above* the navigation graph, so there is a
  shell-level `AppViewModel` on both platforms holding only `themeMode`.
- Favourites are addressable by id alone, so a favourite whose station is not cached shows up in the
  count but not the list until its province is loaded again. That is visible to the user rather than
  silent.
- `FuelioDatabase` is now at version 2 with a real migration and a committed `2.json`. Every future
  entity change needs the same.

## Excluded from this work

- **Firebase Performance Monitoring.** `perf-plugin` has a documented incompatibility with AGP 8.12+
  (firebase-android-sdk#7092) and this project is on AGP 9.2.1. Crashlytics — which needs no Gradle
  plugin beyond the one already applied — is wired instead. Revisit when the plugin ships an
  AGP 9-compatible release.
