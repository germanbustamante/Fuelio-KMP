package com.germandebustamante.fuelio.core.util

expect fun Double.format(digits: Int): String

fun Double.formatAsEuros(): String = "${format(3)} €/L"

fun Double.formatAsKilometers(): String = "${format(1)} km"
