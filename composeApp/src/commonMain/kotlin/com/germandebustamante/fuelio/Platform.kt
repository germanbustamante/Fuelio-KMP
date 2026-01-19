package com.germandebustamante.fuelio

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform