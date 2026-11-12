package gt.electropro.domain.calculations

data class StandardConductorSize(val designation: String, val circularMils: Double)

object ConductorSizeCatalog {
    val sizes = listOf(
        StandardConductorSize("14 AWG", 4_110.0),
        StandardConductorSize("12 AWG", 6_530.0),
        StandardConductorSize("10 AWG", 10_380.0),
        StandardConductorSize("8 AWG", 16_510.0),
        StandardConductorSize("6 AWG", 26_240.0),
        StandardConductorSize("4 AWG", 41_740.0),
        StandardConductorSize("3 AWG", 52_620.0),
        StandardConductorSize("2 AWG", 66_360.0),
        StandardConductorSize("1 AWG", 83_690.0),
        StandardConductorSize("1/0 AWG", 105_600.0),
        StandardConductorSize("2/0 AWG", 133_100.0),
        StandardConductorSize("3/0 AWG", 167_800.0),
        StandardConductorSize("4/0 AWG", 211_600.0),
        StandardConductorSize("250 kcmil", 250_000.0),
        StandardConductorSize("300 kcmil", 300_000.0),
        StandardConductorSize("350 kcmil", 350_000.0),
        StandardConductorSize("400 kcmil", 400_000.0),
        StandardConductorSize("500 kcmil", 500_000.0),
        StandardConductorSize("600 kcmil", 600_000.0),
        StandardConductorSize("750 kcmil", 750_000.0),
        StandardConductorSize("1000 kcmil", 1_000_000.0)
    )
}
