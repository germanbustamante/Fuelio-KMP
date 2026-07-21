package com.germandebustamante.fuelio.core.domain.gasstation.model

enum class GasStationBrand(private val alias: String) {
    REPSOL("REPSOL"),
    CEPSA("CEPSA"),
    BP("BP"),
    SHELL("SHELL"),
    GALP("GALP"),
    ALCAMPO("ALCAMPO"),
    PETRONOR("PETRONOR"),
    PETROPRIX("PETROPRIX"),
    Q8("Q8"),
    BALLENOIL("BALLENOIL"),
    MOEVE("MOEVE"),
    NATURGY("NATURGY"),
    CARREFOUR("CARREFOUR"),
    COSTCO("COSTCO"),
    PLENERGY("PLENERGY"),
    GACOSUR("GACOSUR"),
    ;

    companion object {
        private val wordSeparator = Regex("[^A-Z0-9]+")

        fun fromName(name: String): GasStationBrand? {
            val words = name.uppercase().split(wordSeparator).filter { it.isNotEmpty() }
            return entries.firstOrNull { brand -> brand.alias in words }
        }
    }
}
