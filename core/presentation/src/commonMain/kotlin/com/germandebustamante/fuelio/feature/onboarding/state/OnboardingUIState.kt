package com.germandebustamante.fuelio.feature.onboarding.state

import com.germandebustamante.fuelio.core.domain.preferences.model.FuelType

// Stability declared in androidApp/compose_stability.conf — this module can't depend on Compose.
data class OnboardingUIState(val step: OnboardingStep = OnboardingStep.WELCOME, val selectedFuelType: FuelType = FuelType.GASOLINE_95)
