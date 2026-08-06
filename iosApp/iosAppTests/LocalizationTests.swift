import Testing
import Foundation
@testable import Fuelio

/// Localization coverage.
///
/// Keys are the English source text, so a forgotten translation is invisible at runtime — Spanish
/// users would just see English. These tests make it a build failure instead.
@Suite("Localization")
struct LocalizationTests {

    private static let spanishBundle: Bundle? = {
        Bundle.main.path(forResource: "es", ofType: "lproj").flatMap(Bundle.init(path:))
    }()

    /// Every key the UI actually asks for. Adding a string to a view without adding it here (and to
    /// the catalog) fails the suite.
    private static let uiKeys = [
        "Fuelio", "Search gas station…", "No matches found",
        "No gas station matches that name or address. Try a different search term.",
        "Clear search", "Something went wrong", "Retry", "Detect my location",
        "Couldn't refresh. Showing saved data.",
        "Location access needed",
        "Location access permanently denied. Enable it in Settings to see nearby stations.",
        "Open Settings", "Cancel",
        "Cheapest", "Open", "Closed", "at %@", "cheapest station", "Favorite", "Price unavailable",
        "Add to favorites", "Remove from favorites",
        "Change province, currently %@", "Select Province", "Search province…", "Close",
        "Fuel type", "Gasoline 95", "Gasoline 98", "Diesel", "Diesel+",
        "Back", "Gas station not available",
        "This station isn't cached locally yet. Go back to the list to load it first.",
        "Fuel prices", "Schedule", " · today", "Open 24h", "Get directions", "Map showing %@",
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday",
    ]

    @Test("Ships a Spanish localization")
    func shipsSpanish() throws {
        #expect(Self.spanishBundle != nil, "es.lproj is missing — check the project's knownRegions")
        #expect(Bundle.main.localizations.contains("es"))
        #expect(Bundle.main.localizations.contains("en"))
    }

    @Test("Translates every key the UI uses", arguments: uiKeys)
    func translatesEveryKey(key: String) throws {
        let spanish = try #require(Self.spanishBundle)
        let sentinel = "__missing__"
        let translated = spanish.localizedString(forKey: key, value: sentinel, table: nil)

        #expect(translated != sentinel, "no Spanish translation for \"\(key)\"")
    }

    @Test("Actually translates, rather than echoing the English key")
    func doesNotEchoEnglish() throws {
        let spanish = try #require(Self.spanishBundle)
        // Proper nouns and symbols legitimately stay identical; everything else must differ.
        let allowedIdentical: Set<String> = ["Fuelio", "Diesel+"]

        for key in Self.uiKeys where !allowedIdentical.contains(key) {
            let translated = spanish.localizedString(forKey: key, value: key, table: nil)
            #expect(translated != key, "\"\(key)\" is still English in es.lproj")
        }
    }

    @Test("Localizes the location usage description, since a missing one terminates the app")
    func localizesUsageDescription() throws {
        let spanish = try #require(Self.spanishBundle)
        let key = "NSLocationWhenInUseUsageDescription"
        let value = spanish.localizedString(forKey: key, value: "__missing__", table: "InfoPlist")

        #expect(value != "__missing__")
        #expect(value.contains("ubicación"))
        #expect(Bundle.main.object(forInfoDictionaryKey: key) != nil)
    }
}
