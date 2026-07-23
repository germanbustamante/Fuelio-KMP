package com.germandebustamante.fuelio.core.util

import kotlinx.datetime.LocalTime

fun LocalTime.asHhMm(): String = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
