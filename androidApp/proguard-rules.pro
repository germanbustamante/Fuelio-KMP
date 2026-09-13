# Deliberately empty, not an oversight — verified against a real `:androidApp:assembleRelease` run
# (2026-09-14) rather than left untested with isMinifyEnabled=true:
#
# - R8 produced no `missing_rules.txt` and the build itself only fails on unresolved references, so
#   a clean build is a real (if partial) signal here, not merely "it compiled."
# - Koin is wired with plain DSL (`single {}` / `factory {}` / `viewModel {}`), never
#   `koin-annotations` — see docs/adr/0007-koin-over-hilt.md. Those lambdas call constructors
#   directly in bytecode; there is no `Class.forName`/reflective construction for R8 to break.
# - kotlinx.serialization ships its own consumer rules for `@Serializable` types (the back stack's
#   `Destination`/`DestinationNavKey` polymorphic hierarchy) as part of its Gradle plugin; confirmed
#   in `mapping.txt` that `_childSerializers$_anonymous_()` and `Destination$GasStationDetails`
#   survive shrinking with their generated serializer intact.
# - Room's DAOs are KSP-generated concrete classes that call entity fields directly, not through
#   reflection, so no keep rule is needed there either.
#
# Add narrow, specific rules here only if a future release build's R8 run actually reports one
# missing — never a broad library-wide rule pre-emptively (see CLAUDE.md's R8 section).
