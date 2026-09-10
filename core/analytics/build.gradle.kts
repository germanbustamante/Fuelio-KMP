import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.buildkonfig)
}

// Third-party API keys (currently just PostHog) live in thirdparties.properties, gitignored,
// separate from local.properties (which Android Studio owns for the SDK path). Add
// "POSTHOG_API_KEY=phc_xxx" there locally, or set the POSTHOG_API_KEY env var in CI. Missing/blank
// is a valid, supported state: PostHogTracker.getPostHogTracker() returns null and PostHog stays off.
// The generated AnalyticsSecrets object is shared by every target (Android, iOS, and eventually Web),
// so there's a single source of truth for this module's third-party keys instead of one per platform.
val thirdPartiesProperties = Properties().apply {
    val thirdPartiesPropertiesFile = rootProject.file("thirdparties.properties")
    if (thirdPartiesPropertiesFile.exists()) {
        thirdPartiesPropertiesFile.inputStream().use { load(it) }
    }
}
val postHogApiKey: String =
    (thirdPartiesProperties.getProperty("POSTHOG_API_KEY") ?: System.getenv("POSTHOG_API_KEY")).orEmpty()

buildkonfig {
    packageName = "com.germandebustamante.fuelio.core.analytics"
    exposeObjectWithName = "AnalyticsSecrets"

    defaultConfigs {
        buildConfigField(STRING, "POSTHOG_API_KEY", postHogApiKey)
    }
}

kotlin {
    androidLibrary {
        namespace = "com.germandebustamante.fuelio.core.analytics"
        compileSdk = libs.versions.android.targetSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        // Runs commonTest on the JVM as `:core:analytics:testAndroidHostTest`. Without it these tests
        // are only reachable through iosSimulatorArm64Test, which needs a macOS machine.
        withHostTestBuilder {
        }
    }

    val xcfName = "coreAnalyticsKit"

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
            }
        }

        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.analytics)
                implementation(libs.firebase.crashlytics)
                implementation(libs.posthog.android)
                implementation(libs.koin.android)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
