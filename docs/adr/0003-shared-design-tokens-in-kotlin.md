# 0003 — Shared design tokens in Kotlin (`:core:presentation`)

- **Status:** Accepted
- **Date:** 2026-08-02
- **Depends on:** [0001](0001-kmp-with-native-ui-instead-of-compose-multiplatform.md),
  [0002](0002-core-presentation-module-without-compose.md)

## Context

After migrating iOS to native SwiftUI, the UI stops being shared between platforms — each has its own
component tree (Jetpack Compose in `androidApp/designsystem/`, native SwiftUI on iOS). But the brand
identity (color palette, spacing scale, radii, typography) should still be the same on both: it's "one
design system, two native implementations," not two systems that happen to coincide.

The concrete *values* (`DeepOrange600 = 0xFFF4511E`, `md = 16dp`, `radius.medium = 12dp`, …) until now
only existed as Kotlin literals in `composeApp/core/ui/theme/`. Porting them to SwiftUI by hand
introduces a real risk of silent drift: nothing stops someone from changing a value on one side and
forgetting the other.

## Alternatives considered

1. **Duplicate by hand.** Swift files with the same values, a comment pointing at the source Kotlin
   constant, plus this same ADR documenting that the duplication is deliberate. The simplest option,
   no new dependencies — but the only guard against drift is developer discipline. Nothing fails at
   compile time if a value diverges.
2. **Style Dictionary (tokens.json + codegen).** The industry standard for cross-platform design
   tokens: define tokens once in JSON (W3C Design Tokens format), a Node build step generates Kotlin and
   Swift. It's the only path that would connect to Figma if the design↔code loop ever gets closed.
   Rejected for now: it introduces a Node toolchain into a project that's otherwise pure Gradle + Xcode,
   for a benefit (Figma integration) that isn't needed yet.
3. **Shared Kotlin in `:core:presentation`** — chosen.

## Decision

Raw tokens (color, spacing, radius, type size/weight) are declared as primitives in
`core/presentation/src/commonMain/.../core/designtokens/` (`FuelioColorTokens`, `FuelioSpacingTokens`,
`FuelioRadiusTokens`, `FuelioTypeTokens`), using `object` + `const val` so Kotlin/Native exports them as
static constants readable from Swift (`FuelioColorTokens.shared.ACCENT_LIGHT`). Each platform writes a
thin adapter on top of those primitives:

- Android: `androidApp/core/ui/theme/` — `Color.kt`/`Spacing.kt`/`Shape.kt`/`Typography.kt` keep their
  public API unchanged; only where the number comes from changes.
- iOS: `iosApp/iosApp/DesignSystem/` — `FuelioColors`, `FuelioSpacing`, `FuelioRadius`, `Font.fuelio(...)`.

This gives a single source of truth that's **verifiable at compile time** (if the token doesn't exist,
the build fails on both platforms) with no new tooling, reusing the KMP framework that
[0002](0002-core-presentation-module-without-compose.md) already sets up to export to iOS.

### What's shared, and what isn't, deliberately

- **Color:** only the brand ramps and a subset of semantic roles that make sense on both platforms
  (`accent`, `success`, `danger`, `background`, `surface`, `onSurface`, `outline`). Material-specific
  roles (`primaryContainer`, `onSurfaceVariant`, `surfaceTint`, `inversePrimary`…) do **not** cross over
  to Swift — on iOS those are resolved with system colors/materials (`.secondary`, `.regularMaterial`,
  `Color(.systemGroupedBackground)`).
- **Typography:** base size and weight per role are shared, but iOS applies them on top of Dynamic Type
  semantic styles (`Font.custom(_:size:relativeTo:)`) instead of Material's 15 fixed sizes — so text on
  iOS respects the system text-size setting, the expected native behavior, which Android doesn't need to
  rescale the same way here.
- **Elevation:** not shared at all. Material's tonal elevation has no direct iOS equivalent; depth there
  is expressed with materials and grouping instead.
- **Components:** never shared. Each platform uses its own native controls with the tokens applied — the
  goal is the same brand, not a pixel-perfect SwiftUI replica of the Composables.

## Consequences

- Adding or changing a token is a single commit in `:core:presentation` that both platforms pick up on
  their next build; there's no manual sync step.
- If integration with design tooling (Figma tokens, etc.) is ever needed, the natural migration is to
  Style Dictionary generating into this same `core/designtokens/` as one of its output targets, not a
  redesign from scratch.
- A new contributor needs to know that **a new token value is added first, never hardcoded directly in
  a platform adapter** — see the "Design System" section of `CLAUDE.md`.
