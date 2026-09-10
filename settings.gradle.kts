rootProject.name = "Fuelio"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

// Robolectric needs a Java 21 VM to open an SDK 37 sandbox (see androidApp's testOptions), while the
// build itself still targets JVM 11. Rather than making every contributor install a second JDK by
// hand, let Gradle resolve and provision the toolchain it asks for.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":data")
include(":core:domain")
include(":core:analytics")
include(":core:presentation")
