import SwiftUI
import UIKit

extension Color {
    /// Builds a `Color` from a packed ARGB value, matching the `Long` format used by
    /// `FuelioColorTokens` in `:core:presentation` (e.g. `0xFFF4511E`).
    init(argb: Int64) {
        let a = Double((argb >> 24) & 0xFF) / 255.0
        let r = Double((argb >> 16) & 0xFF) / 255.0
        let g = Double((argb >> 8) & 0xFF) / 255.0
        let b = Double(argb & 0xFF) / 255.0
        self.init(.sRGB, red: r, green: g, blue: b, opacity: a)
    }

    /// Resolves to `light` or `dark` depending on the system appearance, mirroring
    /// `isSystemInDarkTheme()` on the Compose side.
    static func dynamic(light: Int64, dark: Int64) -> Color {
        Color(uiColor: UIColor { traits in
            traits.userInterfaceStyle == .dark ? UIColor(Color(argb: dark)) : UIColor(Color(argb: light))
        })
    }
}
