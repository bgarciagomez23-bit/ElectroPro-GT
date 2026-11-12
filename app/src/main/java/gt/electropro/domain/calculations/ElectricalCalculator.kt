package gt.electropro.domain.calculations

import kotlin.math.sqrt

/**
 * Basic AC power calculations for balanced, sinusoidal systems.
 *
 * Three-phase calculations use line-to-line voltage and assume a balanced load.
 * Results are ideal electrical quantities; they do not perform code-based
 * conductor or overcurrent-device sizing.
 */
enum class SystemPhase {
    SINGLE_PHASE,
    THREE_PHASE
}

data class PowerQuantities(
    val apparentPowerVA: Double,
    val activePowerW: Double,
    val reactivePowerVar: Double,
    val powerFactor: Double
)

object ElectricalCalculator {
    private const val SQRT_THREE = 1.7320508075688772

    /**
     * Calculates line current from active power.
     * For three-phase systems, voltageV must be line-to-line voltage.
     */
    fun currentFromActivePower(
        activePowerW: Double,
        voltageV: Double,
        powerFactor: Double,
        phase: SystemPhase
    ): Double {
        requirePositiveFinite("activePowerW", activePowerW)
        requirePositiveFinite("voltageV", voltageV)
        requirePowerFactor(powerFactor)

        val denominator = when (phase) {
            SystemPhase.SINGLE_PHASE -> voltageV * powerFactor
            SystemPhase.THREE_PHASE -> SQRT_THREE * voltageV * powerFactor
        }
        return activePowerW / denominator
    }

    /**
     * Calculates P, Q and S from RMS voltage/current and power factor.
     * For three-phase systems, voltageV is line-to-line and currentA is line current.
     */
    fun powerFromVoltageCurrent(
        voltageV: Double,
        currentA: Double,
        powerFactor: Double,
        phase: SystemPhase
    ): PowerQuantities {
        requirePositiveFinite("voltageV", voltageV)
        requirePositiveFinite("currentA", currentA)
        requirePowerFactor(powerFactor)

        val apparentPower = when (phase) {
            SystemPhase.SINGLE_PHASE -> voltageV * currentA
            SystemPhase.THREE_PHASE -> SQRT_THREE * voltageV * currentA
        }
        val activePower = apparentPower * powerFactor
        val reactivePower = sqrt(
            (apparentPower * apparentPower - activePower * activePower).coerceAtLeast(0.0)
        )

        return PowerQuantities(
            apparentPowerVA = apparentPower,
            activePowerW = activePower,
            reactivePowerVar = reactivePower,
            powerFactor = powerFactor
        )
    }

    private fun requirePositiveFinite(name: String, value: Double) {
        require(value.isFinite() && value > 0.0) {
            "$name must be a finite value greater than zero"
        }
    }

    private fun requirePowerFactor(value: Double) {
        require(value.isFinite() && value > 0.0 && value <= 1.0) {
            "powerFactor must be greater than zero and less than or equal to one"
        }
    }
}
