# 0008 — Ktor over Retrofit for the HTTP client

- **Status:** Accepted
- **Date:** 2026-09-14

## Context

`:data` needs one HTTP client to fetch provinces and gas-station listings from the MINETUR open-data
API, shared between Android and iOS. Retrofit is the dominant HTTP client on Android — declarative
service interfaces, OkHttp under the hood, a large ecosystem of converters and interceptors — and is
what most Android-only codebases already default to.

Retrofit is JVM-only: it is built directly on `java.lang.reflect` dynamic proxies to implement the
annotated service interfaces at runtime, and OkHttp (its transport) has no Kotlin/Native target
either. `:data` compiles to `iosArm64`/`iosSimulatorArm64` in addition to the JVM, so Retrofit could
not produce even a single common interface — the request/response types, serialization and error
handling would all have to be written twice.

## Decision

Use Ktor's multiplatform client (`io.ktor:ktor-client-*`, all pinned in `gradle/libs.versions.toml`
to `3.5.1`) with a platform-selected engine behind `expect`/`actual`.

`data/src/*/kotlin/.../engine/HttpClientEngineProvider.kt` declares an `expect` function that each
platform source set implements against its fastest native transport: OkHttp on Android
(`ktor-client-okhttp`), Darwin's `NSURLSession` on iOS (`ktor-client-darwin`). Everything above the
engine — the `HttpClient` construction, `ContentNegotiation` with `kotlinx.serialization.json`, the
`GasStationRemoteDataSourceImpl`/`ProvinceRemoteDataSourceImpl` request builders, and the DTOs — is
one `commonMain` implementation. Tests substitute `ktor-client-mock`'s `MockEngine` for the real
engine (`data/src/commonTest/.../util/BaseRemoteDataSourceTest.kt`), so the same request-building
code is exercised in `commonTest` regardless of which real engine would have handled it on device.

## Alternatives considered

**Retrofit for Android, `URLSession` calls by hand for iOS.** Rejected for the same reason as the
Hilt alternative in [0007](0007-koin-over-hilt.md): two networking stacks means the request
construction, JSON parsing and error mapping are written twice and can silently diverge — a field
added to `GasStationDTO` on one platform and forgotten on the other would fail only at runtime, and
only on the platform that was missed.

**`kotlinx-io`/raw socket handling instead of an HTTP client library.** Not seriously considered:
this is a REST client against a single JSON API, not a protocol implementation; Ktor already covers
content negotiation, timeouts and engine selection, which would otherwise be rebuilt by hand for no
benefit.

## Consequences

- One request-building and JSON-parsing implementation in `commonMain`, verified once against
  `MockEngine` instead of twice against two engine-specific test doubles.
- The engine choice is the only platform-specific line in the entire networking stack — a genuine
  `expect`/`actual` seam, not a re-implementation. Swapping either platform's transport (say, moving
  Android off OkHttp) touches exactly one file.
- Ktor's client API is less familiar to developers coming from Retrofit-only Android codebases, and
  its annotation-free, builder-style request construction (`client.get(url) { ... }` instead of a
  declarative `@GET` interface) is a small ramp-up cost with no compile-time-checked route/parameter
  binding — Retrofit's interface-based approach would have to be manually replicated as tests
  instead.
