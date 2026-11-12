package gt.electropro.domain.calculations

import kotlin.math.sqrt

enum class TransformerPhase { SINGLE_PHASE, THREE_PHASE }

data class TransformerCalculationResult(
    val primaryCurrentA: Double,
    val secondaryCurrentA: Double,
    val loadPercent: Double,
    val secondaryFaultCurrentKA: Double,
    val transformerImpedancePercent: Double
)

object TransformerCalculator {
    fun calculate(
        ratedPowerKva: Double,
        primaryVoltageV: Double,
        secondaryVoltageV: Double,
        impedancePercent: Double,
        loadKva: Double,
        phase: TransformerPhase
    ): TransformerCalculationResult {
        listOf(ratedPowerKva, primaryVoltageV, secondaryVoltageV, impedancePercent)
            .forEach { require(it.isFinite() && it > 0.0) { "Potencia, tensiones e impedancia deben ser mayores que cero." } }
        require(loadKva.isFinite() && loadKva >= 0.0) { "La carga debe ser un valor no negativo." }
        val divisor = if (phase == TransformerPhase.THREE_PHASE) sqrt(3.0) else 1.0
        val primary = ratedPowerKva * 1000.0 / (divisor * primaryVoltageV)
        val secondary = ratedPowerKva * 1000.0 / (divisor * secondaryVoltageV)
        val loadPercent = loadKva / ratedPowerKva * 100.0
        // Transformer-only, bolted terminal fault estimate; upstream source impedance is neglected.
        val secondaryFaultKa = secondary * (100.0 / impedancePercent) / 1000.0
        return TransformerCalculationResult(
            primaryCurrentA = primary,
            secondaryCurrentA = secondary,
            loadPercent = loadPercent,
            secondaryFaultCurrentKA = secondaryFaultKa,
            transformerImpedancePercent = impedancePercent
        )
    }
}
