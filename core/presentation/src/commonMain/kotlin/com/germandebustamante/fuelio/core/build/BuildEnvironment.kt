package com.germandebustamante.fuelio.core.build

/**
 * What kind of build this is, resolved once per platform.
 *
 * Provided by `presentationPlatformModule` rather than by an `expect object`, because both answers
 * come from something the platform module already has: Android reads the debuggable flag off the
 * `Context` Koin is holding, iOS reads `Platform.isDebugBinary`. Keeping it a plain data class means
 * a test can construct whichever combination it needs.
 */
data class BuildEnvironment(
    val isDebug: Boolean,
    val platform: String,
) {
    companion object {
        const val PLATFORM_ANDROID = "android"
        const val PLATFORM_IOS = "ios"
    }
}
