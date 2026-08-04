# Fuelio

A gas-station finder built with Kotlin Multiplatform: shared domain/data/presentation logic in
Kotlin, **native UI on each platform** — Jetpack Compose on Android, SwiftUI on iOS. See
[`docs/adr/`](./docs/adr) for the architectural decisions behind that split, and `CLAUDE.md` for the
full module-by-module reference.

## Modules

```
:core:domain        →  Business logic only (no platform deps)
:core:analytics      →  Analytics tracking abstraction (Trace/Trackable/AnalyticsManager) + Firebase/PostHog
:data                →  Repository implementations, Ktor HTTP client, Room local persistence
:core:presentation   →  ViewModels, UIState, Navigator/Destination, DI modules, shared design tokens — no Compose
:androidApp          →  Jetpack Compose UI (Android-only application module)
iosApp (Xcode)       →  SwiftUI UI, consumes the CorePresentation.framework built from :core:presentation
```

`:core:presentation` is the module that gets exported as a Kotlin/Native framework
(`CorePresentation.framework`) for Xcode to link — it has zero Compose/Navigation3 dependencies by
design (see [ADR 0002](./docs/adr/0002-core-presentation-module-without-compose.md)).

## Build and run — Android

```shell
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug
```

Or use the run configuration from the IDE's toolbar.

## Build and run — iOS

Open [`iosApp/iosApp.xcodeproj`](./iosApp/iosApp.xcodeproj) in Xcode and run from there. The
"Compile Kotlin Framework" build phase invokes
`./gradlew :core:presentation:embedAndSignAppleFrameworkForXcode` automatically, so no manual Gradle
step is needed first.

To sanity-check the framework outside Xcode:

```shell
./gradlew :core:presentation:linkDebugFrameworkIosSimulatorArm64
```

## Tests

```shell
./gradlew :androidApp:testDebugUnitTest              # Compose-dependent code (designsystem, screens)
./gradlew :core:presentation:testAndroidHostTest      # ViewModel/state/navigation tests, JVM
./gradlew :core:presentation:iosSimulatorArm64Test    # Same tests, iOS simulator target
./gradlew :core:domain:iosSimulatorArm64Test
./gradlew :data:iosSimulatorArm64Test
./gradlew :core:analytics:iosSimulatorArm64Test
```

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
