# 0001 — KMP with native UI (SwiftUI + Jetpack Compose) instead of Compose Multiplatform

- **Status:** Accepted
- **Date:** 2026-08-01

## Context

Fuelio started as a Kotlin Multiplatform project using **Compose Multiplatform** for the entire UI,
including iOS: `composeApp` compiled for both Android and iOS, and `MainViewController.kt` hosted a
`ComposeUIViewController` inside the Xcode app. This is a stable, supported setup — Compose
Multiplatform for iOS has been GA since May 2025 (v1.8.0) — and that's how the app worked before this
decision.

The project's stated goal is to serve as a portfolio piece for senior Android/iOS/KMP interview
processes. Reviewing the 2026 job market, two signals changed the calculus:

- The iOS roles German sees more frequently ask for SwiftUI explicitly, more often than Compose
  Multiplatform.
- The 2026 industry consensus on KMP is "never start by sharing UI, start by sharing logic" — KMP was
  designed for domain/data/networking, not for replacing Swift on screen. The pattern most requested in
  job postings is: shared Kotlin logic (domain, data, ViewModels) + Jetpack Compose on Android + SwiftUI
  on iOS.

## Decision

Migrate the iOS UI from Compose Multiplatform to native SwiftUI, keeping Jetpack Compose on Android
and all logic (domain, data, ViewModels, navigation, analytics) shared in Kotlin.

The already-built Compose visual layer for iOS is discarded — real work thrown away — in exchange for
demonstrating actual SwiftUI, and for access to 100% of iOS's native APIs (accessibility, text
selection gestures, performance) without the simulation layer Compose Multiplatform puts on top of
UIKit.

## Alternatives considered

**Stay on Compose Multiplatform (shared UI).** A single UI codebase, pixel-perfect parity across
platforms, very fast iteration, zero migration cost. Rejected because it doesn't demonstrate SwiftUI —
the gap that shows up most often in the roles German is seeing — and because for Android-only
processes it doesn't add anything over plain Jetpack Compose either (same framework, without the added
complexity of multiplatform).

## Consequences

- The architecture already in place isn't wasted: a pure domain layer, `expect`/`actual`, and
  ViewModels with no Compose dependency are exactly what's needed to separate logic from UI — see
  [0002](0002-core-presentation-module-without-compose.md).
- A real Kotlin↔Swift bridge is needed so SwiftUI can consume `StateFlow`/sealed classes from the
  Kotlin/Native framework (`CorePresentation.framework`).
- Two UIs to maintain in parallel: every new UI feature gets implemented twice, once per platform. This
  is the explicit cost of this decision.
- Design tokens (color, typography, spacing) are shared in Kotlin so both native UIs stay "the same
  system, two implementations" — see [0003](0003-shared-design-tokens-in-kotlin.md).
