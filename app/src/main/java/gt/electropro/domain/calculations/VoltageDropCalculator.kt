package gt.electropro.domain.calculations

enum class ConductorMaterial(val kOhmCircularMilsPerFoot: Double) {
    COPPER_75C(12.9),
    ALUMINUM_75C(21.2)
}

data class VoltageDropResult(
    val voltageDropV: Double,
    val voltageDropPercent: Double,
    val receivingVoltageV: Double
)

/**
 * Approximate resistive voltage-drop estimate using K in ohm-circular-mil/ft.
 * Input length is one-way route length in meters and is converted to feet internally.
 * Area is conductor circular mils. This does not replace impedance-based calculations
 * or NEC ampacity sizing.
 */
object VoltageDropCalculator {
    private const val METERS_PER_FOOT = 0.3048

    fun calculate(
        currentA: Double,
        oneWayLengthM: Double,
        systemVoltageV: Double,
        conductorAreaCircularMils: Double,
        material: ConductorMaterial,
        phase: SystemPhase
    ): VoltageDropResult {
        requirePositive("currentA", currentA)
        requirePositive("oneWayLengthM", oneWayLengthM)
        requirePositive("systemVoltageV", systemVoltageV)
        requirePositive("conductorAreaCircularMils", conductorAreaCircularMils)

        val oneWayLengthFt = oneWayLengthM / METERS_PER_FOOT
        val multiplier = when (phase) {
            SystemPhase.SINGLE_PHASE -> 2.0
            SystemPhase.THREE_PHASE -> 1.7320508075688772
        }
        val drop = multiplier * material.kOhmCircularMilsPerFoot *
            currentA * oneWayLengthFt / conductorAreaCircularMils
        val percent = drop / systemVoltageV * 100.0

        return VoltageDropResult(
            voltageDropV = drop,
            voltageDropPercent = percent,
            receivingVoltageV = systemVoltageV - drop
        )
    }

    private fun requirePositive(name: String, value: Double) {
        require(value.isFinite() && value > 0.0) {
            "$name must be a finite value greater than zero"
        }
    }
}
