import SwiftUI
import CorePresentation
import KMPObservableViewModelSwiftUI

/// Same split as every other screen: the stateful half owns the Kotlin ViewModel, the stateless
/// `OnboardingScreenBody` renders, so `#Preview` never resolves anything from Koin.
struct OnboardingScreen: View {

    @StateViewModel private var viewModel = IosViewModelFactory.shared.onboarding()

    var body: some View {
        OnboardingScreenBody(
            state: viewModel.state,
            onNextTapped: { viewModel.onWelcomeNextTapped() },
            onSkipTapped: { viewModel.onSkipAllTapped() },
            onAllowLocationTapped: { viewModel.onRequestLocationPermissionTapped() },
            onSkipLocationTapped: { viewModel.onSkipLocationPermissionTapped() },
            onFuelTypeSelected: { viewModel.onFuelTypeSelected(fuelType: $0) },
            onFinishTapped: { viewModel.onFinishTapped() }
        )
    }
}

struct OnboardingScreenBody: View {

    let state: OnboardingUIState
    let onNextTapped: () -> Void
    let onSkipTapped: () -> Void
    let onAllowLocationTapped: () -> Void
    let onSkipLocationTapped: () -> Void
    let onFuelTypeSelected: (DomainFuelType) -> Void
    let onFinishTapped: () -> Void

    var body: some View {
        VStack(spacing: FuelioSpacing.lg) {
            Spacer()
            switch state.step {
            case .welcome:
                welcomeStep
            case .locationPermission:
                locationPermissionStep
            case .defaultFuel:
                defaultFuelStep
            }
            Spacer()
        }
        .padding(FuelioSpacing.xl)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(FuelioColors.background)
        // Without `.contain`, this container's identifier overwrites every descendant's own
        // identifier (the next/skip/allow-location buttons included) — same fix as `FavoritesScreen`.
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(A11yID.onboardingScreen)
        .animation(.smooth(duration: 0.25), value: state.step)
    }

    private var welcomeStep: some View {
        VStack(spacing: FuelioSpacing.lg) {
            Image(systemName: "fuelpump.circle.fill")
                .font(.system(size: 64))
                .foregroundStyle(FuelioColors.accent)
            Text("Find the cheapest fuel near you")
                .font(.fuelio(.title2, weight: .semibold))
                .multilineTextAlignment(.center)
            Text("Fuelio compares prices at every gas station in your province, so you always know where to fill up for less.")
                .font(.fuelio(.body))
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
            VStack(spacing: FuelioSpacing.sm) {
                Button("Next", action: onNextTapped)
                    .buttonStyle(.borderedProminent)
                    .frame(maxWidth: .infinity)
                    .accessibilityIdentifier(A11yID.onboardingNextButton)
                Button("Skip", action: onSkipTapped)
                    .buttonStyle(.plain)
                    .frame(maxWidth: .infinity)
                    .accessibilityIdentifier(A11yID.onboardingSkipButton)
            }
        }
    }

    private var locationPermissionStep: some View {
        VStack(spacing: FuelioSpacing.lg) {
            Image(systemName: "location.circle.fill")
                .font(.system(size: 64))
                .foregroundStyle(FuelioColors.accent)
            Text("Find stations near you")
                .font(.fuelio(.title2, weight: .semibold))
                .multilineTextAlignment(.center)
            Text("Allow location access to sort gas stations by distance. You can always change this later in Settings.")
                .font(.fuelio(.body))
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
            VStack(spacing: FuelioSpacing.sm) {
                Button("Allow location access", action: onAllowLocationTapped)
                    .buttonStyle(.borderedProminent)
                    .frame(maxWidth: .infinity)
                    .accessibilityIdentifier(A11yID.onboardingAllowLocationButton)
                Button("Skip", action: onSkipLocationTapped)
                    .buttonStyle(.plain)
                    .frame(maxWidth: .infinity)
                    .accessibilityIdentifier(A11yID.onboardingSkipButton)
            }
        }
    }

    private var defaultFuelStep: some View {
        VStack(spacing: FuelioSpacing.lg) {
            Text("What do you usually fill up with?")
                .font(.fuelio(.title2, weight: .semibold))
                .multilineTextAlignment(.center)
            Text("The list will be sorted and priced by this fuel. You can change it anytime in Settings.")
                .font(.fuelio(.body))
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
            DefaultFuelPicker(
                selected: state.selectedFuelType,
                onSelect: onFuelTypeSelected,
                accessibilityID: A11yID.onboardingFuelPicker
            )
            Button("Get started", action: onFinishTapped)
                .buttonStyle(.borderedProminent)
                .frame(maxWidth: .infinity)
                .accessibilityIdentifier(A11yID.onboardingFinishButton)
        }
    }
}

#Preview("Welcome") {
    OnboardingScreenBody(
        state: OnboardingFakesKt.fakeOnboardingUIStateWelcome,
        onNextTapped: {},
        onSkipTapped: {},
        onAllowLocationTapped: {},
        onSkipLocationTapped: {},
        onFuelTypeSelected: { _ in },
        onFinishTapped: {}
    )
}

#Preview("Location permission") {
    OnboardingScreenBody(
        state: OnboardingFakesKt.fakeOnboardingUIStateLocationPermission,
        onNextTapped: {},
        onSkipTapped: {},
        onAllowLocationTapped: {},
        onSkipLocationTapped: {},
        onFuelTypeSelected: { _ in },
        onFinishTapped: {}
    )
}

#Preview("Default fuel") {
    OnboardingScreenBody(
        state: OnboardingFakesKt.fakeOnboardingUIStateDefaultFuel,
        onNextTapped: {},
        onSkipTapped: {},
        onAllowLocationTapped: {},
        onSkipLocationTapped: {},
        onFuelTypeSelected: { _ in },
        onFinishTapped: {}
    )
}
