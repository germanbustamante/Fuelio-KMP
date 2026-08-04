package com.germandebustamante.fuelio.core.startup

fun interface StartupTask {
    suspend operator fun invoke()
}
