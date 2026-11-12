package gt.electropro.domain.calculations

data class AmpacityCheckResult(
    val requiredAmpacityA: Double,
    val correctedConductorAmpacityA: Double,
    val terminalAllowableAmpacityA: Double,
    val conductorPasses: Boolean,
    val terminalPasses: Boolean
) {
    val overallPasses: Boolean get() = conductorPasses && terminalPasses
}

/**
 * Checks user-supplied NEC table ampacity and correction factors.
 * It intentionally does not embed NEC ampacity tables or assume terminal ratings.
 */
object AmpacityChecker {
    fun calculate(
        nonContinuousLoadA: Double,
        continuousLoadA: Double,
        continuousLoadMultiplier: Double,
        tableAmpacityA: Double,
        ambientCorrectionFactor: Double,
        adjustmentFactor: Double,
        terminalAllowableAmpacityA: Double
    ): AmpacityCheckResult {
        val positiveValues = listOf(
            "tableAmpacityA" to tableAmpacityA,
            "continuousLoadMultiplier" to continuousLoadMultiplier,
            "terminalAllowableAmpacityA" to terminalAllowableAmpacityA
        )
        positiveValues.forEach { (name, value) ->
            require(value.isFinite() && value > 0.0) { "$name debe ser mayor que cero." }
        }
        require(nonContinuousLoadA.isFinite() && nonContinuousLoadA >= 0.0) {
            "La carga no continua no puede ser negativa."
        }
        require(continuousLoadA.isFinite() && continuousLoadA >= 0.0) {
            "La carga continua no puede ser negativa."
        }
        require(nonContinuousLoadA + continuousLoadA > 0.0) { "La carga total debe ser mayor que cero." }
        require(ambientCorrectionFactor.isFinite() && ambientCorrectionFactor > 0.0 && ambientCorrectionFactor <= 1.0) {
            "El factor por temperatura debe estar entre 0 y 1."
        }
        require(adjustmentFactor.isFinite() && adjustmentFactor > 0.0 && adjustmentFactor <= 1.0) {
            "El factor de ajuste debe estar entre 0 y 1."
        }

        val required = nonContinuousLoadA + continuousLoadA * continuousLoadMultiplier
        val corrected = tableAmpacityA * ambientCorrectionFactor * adjustmentFactor
        return AmpacityCheckResult(
            requiredAmpacityA = required,
            correctedConductorAmpacityA = corrected,
            terminalAllowableAmpacityA = terminalAllowableAmpacityA,
            conductorPasses = corrected >= required,
            terminalPasses = terminalAllowableAmpacityA >= required
        )
    }
}
