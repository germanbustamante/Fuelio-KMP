import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
}

//region Constants

private object BuildConstants {
    const val APPLICATION_ID = "com.germandebustamante.fuelio"
    const val DEBUG_APPLICATION_ID_SUFFIX = ".debug"

    // Third-party secrets (Maps, and eventually anything else Android-manifest-only) live in this
    // gitignored file, same convention as core/analytics/build.gradle.kts's POSTHOG_API_KEY.
    const val THIRD_PARTIES_PROPERTIES_FILE_NAME = "thirdparties.properties"
    const val MAPS_API_KEY_PROPERTY = "MAPS_API_KEY"
    const val MAPS_API_KEY_MANIFEST_PLACEHOLDER = "MAPS_API_KEY"

    // keystore.properties is gitignored; release signing is a no-op locally/in CI when it's absent,
    // so the build doesn't break for anyone who hasn't generated a release keystore.
    const val KEYSTORE_PROPERTIES_FILE_NAME = "keystore.properties"
    const val KEYSTORE_STORE_FILE_PROPERTY = "storeFile"
    const val KEYSTORE_STORE_PASSWORD_PROPERTY = "storePassword"
    const val KEYSTORE_KEY_ALIAS_PROPERTY = "keyAlias"
    const val KEYSTORE_KEY_PASSWORD_PROPERTY = "keyPassword"
    const val RELEASE_SIGNING_CONFIG_NAME = "release"
}

//endregion

//region Helpers

private fun loadProperties(file: File): Properties = Properties().apply {
    if (file.exists()) file.inputStream().use { load(it) }
}

//endregion

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

composeCompiler {
    stabilityConfigurationFiles.add(layout.projectDirectory.file("compose_stability.conf"))
}

val thirdPartiesProperties = loadProperties(rootProject.file(BuildConstants.THIRD_PARTIES_PROPERTIES_FILE_NAME))
val keystorePropertiesFile = layout.projectDirectory.file(BuildConstants.KEYSTORE_PROPERTIES_FILE_NAME).asFile
val keystoreProperties = loadProperties(keystorePropertiesFile)
val hasReleaseKeystore = keystorePropertiesFile.exists()

android {
    namespace = BuildConstants.APPLICATION_ID
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }

    defaultConfig {
        applicationId = BuildConstants.APPLICATION_ID
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        // FuelioTestRunner swaps in FuelioTestApplication, which starts Koin with the shared
        // uiTestModule fakes — the Android mirror of iOS's initKoinIosForUiTests, so the
        // instrumentation suite exercises the real app without the network, Room, or a system
        // permission dialog.
        testInstrumentationRunner = "com.germandebustamante.fuelio.FuelioTestRunner"

        // Maps SDK reads its key from this manifest placeholder at runtime, not from Kotlin code,
        // so it can't go through BuildKonfig like POSTHOG_API_KEY.
        manifestPlaceholders[BuildConstants.MAPS_API_KEY_MANIFEST_PLACEHOLDER] =
            thirdPartiesProperties.getProperty(BuildConstants.MAPS_API_KEY_PROPERTY)
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    //region Signing

    signingConfigs {
        if (hasReleaseKeystore) {
            create(BuildConstants.RELEASE_SIGNING_CONFIG_NAME) {
                storeFile = file(keystoreProperties.getProperty(BuildConstants.KEYSTORE_STORE_FILE_PROPERTY))
                storePassword = keystoreProperties.getProperty(BuildConstants.KEYSTORE_STORE_PASSWORD_PROPERTY)
                keyAlias = keystoreProperties.getProperty(BuildConstants.KEYSTORE_KEY_ALIAS_PROPERTY)
                keyPassword = keystoreProperties.getProperty(BuildConstants.KEYSTORE_KEY_PASSWORD_PROPERTY)
            }
        }
    }

    //endregion

    //region Build types

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = BuildConstants.DEBUG_APPLICATION_ID_SUFFIX
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasReleaseKeystore) {
                signingConfig = signingConfigs.getByName(BuildConstants.RELEASE_SIGNING_CONFIG_NAME)
            }
        }
    }

    //endregion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlin.get()
    }
}

dependencies {
    implementation(projects.core.presentation)

    implementation(libs.androidx.core.ktx)

    implementation(libs.runtime)
    implementation(libs.foundation)
    implementation(libs.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.jetbrains.navigation3.ui)
    implementation(libs.jetbrains.material3.adaptiveNavigation3)
    implementation(libs.jetbrains.lifecycle.viewmodelNavigation3)

    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation(libs.anr.watchdog)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)

    debugImplementation(compose.uiTooling)

    testImplementation(libs.kotlin.test)

    androidTestImplementation(libs.ui.test.junit4)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.ui.test.manifest)
}
