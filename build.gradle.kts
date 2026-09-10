import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.jetbrainsKotlinJvm) apply false
    alias(libs.plugins.koin.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.firebaseCrashlytics) apply false
    alias(libs.plugins.kmpNativeCoroutines) apply false
    alias(libs.plugins.skie) apply false
    alias(libs.plugins.ktlint) apply false
}

// ktlint is the one thing every module needs configured identically, so it's applied here instead of
// being repeated in five build files. There is no buildSrc/convention plugin in this project and a
// linter alone doesn't justify introducing one.
// Read out of the catalog here: `libs` is an accessor on the root project only, and inside
// `subprojects { }` the receiver is the subproject, where it doesn't resolve.
val ktlintVersion = libs.versions.ktlint.get()

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extensions.configure<KtlintExtension> {
        version.set(ktlintVersion)
        // Generated sources (Room's KSP output, BuildKonfig's AnalyticsSecrets, the Compose compiler's
        // reports) are never hand-edited and don't follow the style rules.
        filter { exclude { it.file.path.contains("${File.separator}build${File.separator}") } }
        reporters {
            reporter(ReporterType.PLAIN)
            reporter(ReporterType.CHECKSTYLE)
        }
    }
}
