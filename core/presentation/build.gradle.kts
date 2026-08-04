plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.mokkery)
}

kotlin {
    androidLibrary {
        namespace = "com.germandebustamante.fuelio.core.presentation"
        compileSdk = libs.versions.android.targetSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withHostTestBuilder {
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CorePresentation"
            isStatic = true
            export(projects.core.analytics)
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.androidx.lifecycle.viewmodel)
                // api: UI-facing types (GasStationBO, ProvinceBO, DomainError…) are consumed
                // directly by androidApp's Composables via GasStationItemVO/UIState, so they
                // must be visible transitively through :core:presentation.
                api(projects.core.domain)
                implementation(projects.data)
                api(projects.core.analytics)
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.koin.core.viewmodel)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.koin.android)
                implementation(libs.play.services.location)
                implementation(libs.androidx.activity)
                implementation(libs.androidx.core.ktx)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.turbine)
            }
        }
    }
}
