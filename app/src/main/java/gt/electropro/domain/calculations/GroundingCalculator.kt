package gt.electropro.domain.calculations

data class SoilResistivityResult(
    val apparentResistivityOhmM: Double,
    val method: String
)

object SoilResistivityCalculator {
    /** Wenner four-point method: rho = 2*pi*a*R. Spacing and depth in meters. */
    fun wenner(spacingM: Double, measuredResistanceOhm: Double, probeDepthM: Double = 0.0): SoilResistivityResult {
        require(spacingM.isFinite() && spacingM > 0.0)
        require(measuredResistanceOhm.isFinite() && measuredResistanceOhm > 0.0)
        require(probeDepthM.isFinite() && probeDepthM >= 0.0)
        val correction = if (probeDepthM == 0.0) 1.0 else {
            val a = spacingM
            val b = probeDepthM
            (2.0 * a / (a + 2.0 * b)) -
                (a / (2.0 * a + 2.0 * b))
        }
        val rho = 2.0 * Math.PI * spacingM * measuredResistanceOhm / correction
        return SoilResistivityResult(rho, "Wenner cuatro puntos")
    }
}

data class GroundingConductorResult(
    val minimumAreaMm2: Double,
    val selectedAreaMm2: Double,
    val passes: Boolean
)

object GroundingConductorChecker {
    /**
     * Adiabatic screening equation S = I*sqrt(t)/k.
     * k must be selected from verified material/initial/final temperature data.
     */
    fun adiabatic(
        faultCurrentA: Double,
        clearingTimeSeconds: Double,
        kFactor: Double,
        selectedAreaMm2: Double
    ): GroundingConductorResult {
        listOf(faultCurrentA, clearingTimeSeconds, kFactor, selectedAreaMm2).forEach {
            require(it.isFinite() && it > 0.0)
        }
        val required = faultCurrentA * kotlin.math.sqrt(clearingTimeSeconds) / kFactor
        return GroundingConductorResult(required, selectedAreaMm2, selectedAreaMm2 >= required)
    }
}
