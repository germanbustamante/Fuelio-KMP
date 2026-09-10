package com.germandebustamante.fuelio.data.engine

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttpConfig
import io.ktor.client.engine.okhttp.OkHttpEngine

actual fun httpClientEngine(): HttpClientEngine = OkHttpEngine(OkHttpConfig())
