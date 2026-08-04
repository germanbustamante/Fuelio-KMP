import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

composeCompiler {
    stabilityConfigurationFiles.add(layout.projectDirectory.file("compose_stability.conf"))
}

android {
    namespace = "com.germandebustamante.fuelio"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }

    defaultConfig {
        applicationId = "com.germandebustamante.fuelio"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
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
    implementation(libs.anr.watchdog)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)

    debugImplementation(compose.uiTooling)

    testImplementation(libs.kotlin.test)
}
