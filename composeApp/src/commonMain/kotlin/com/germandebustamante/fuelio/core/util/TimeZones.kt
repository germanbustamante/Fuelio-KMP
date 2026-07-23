package com.germandebustamante.fuelio.core.util

import kotlinx.datetime.TimeZone

/** All gas station data comes from the Spanish MINETUR API, so "today"/open-state must be computed in this zone, not the device's local zone. */
val SPAIN_TIMEZONE: TimeZone = TimeZone.of("Europe/Madrid")
