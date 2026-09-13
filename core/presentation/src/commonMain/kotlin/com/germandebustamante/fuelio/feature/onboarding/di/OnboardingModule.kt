package com.germandebustamante.fuelio.feature.onboarding.di

import com.germandebustamante.fuelio.feature.onboarding.state.OnboardingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val onboardingModule = module {
    viewModel { OnboardingViewModel(get(), get(), get(), get()) }
}
