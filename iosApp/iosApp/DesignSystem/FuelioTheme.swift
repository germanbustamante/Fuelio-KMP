import SwiftUI

/// Entry point for the shared design tokens — apply once at the screen root so new screens pull
/// from `FuelioColors`/`FuelioSpacing`/`FuelioRadius`/`Font.fuelio` instead of raw literals.
extension View {
    func fuelioTheme() -> some View {
        self
            .tint(FuelioColors.accent)
            .background(FuelioColors.background)
    }
}
