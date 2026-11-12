package gt.electropro.domain.calculations

data class ProtectionCheckResult(
    val designCurrentA: Double,
    val selectedBreakerA: Double,
    val conductorAllowableAmpacityA: Double,
    val availableFaultCurrentKA: Double,
    val breakerInterruptingRatingKA: Double,
    val loadCurrentPasses: Boolean,
    val conductorCoordinationPasses: Boolean,
    val interruptingCapacityPasses: Boolean
) {
    val overallPasses: Boolean get() =
        loadCurrentPasses && conductorCoordinationPasses && interruptingCapacityPasses
}

object ProtectionChecker {
    val commonBreakerRatingsA = listOf(
        15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0, 60.0, 70.0,
        80.0, 90.0, 100.0, 110.0, 125.0, 150.0, 175.0, 200.0, 225.0,
        250.0, 300.0, 350.0, 400.0, 450.0, 500.0, 600.0, 700.0,
        800.0, 1000.0, 1200.0, 1600.0, 2000.0, 2500.0, 3000.0, 4000.0
    )

    fun calculate(
        nonContinuousLoadA: Double,
        continuousLoadA: Double,
        continuousMultiplier: Double,
        selectedBreakerA: Double,
        conductorAllowableAmpacityA: Double,
        availableFaultCurrentKA: Double,
        breakerInterruptingRatingKA: Double
    ): ProtectionCheckResult {
        require(nonContinuousLoadA.isFinite() && nonContinuousLoadA >= 0.0)
        require(continuousLoadA.isFinite() && continuousLoadA >= 0.0)
        require(nonContinuousLoadA + continuousLoadA > 0.0)
        listOf(
            continuousMultiplier, selectedBreakerA, conductorAllowableAmpacityA,
            availableFaultCurrentKA, breakerInterruptingRatingKA
        ).forEach { require(it.isFinite() && it > 0.0) { "Todos los valores deben ser positivos y finitos." } }

        val design = nonContinuousLoadA + continuousLoadA * continuousMultiplier
        return ProtectionCheckResult(
            designCurrentA = design,
            selectedBreakerA = selectedBreakerA,
            conductorAllowableAmpacityA = conductorAllowableAmpacityA,
            availableFaultCurrentKA = availableFaultCurrentKA,
            breakerInterruptingRatingKA = breakerInterruptingRatingKA,
            loadCurrentPasses = selectedBreakerA >= design,
            conductorCoordinationPasses = conductorAllowableAmpacityA >= selectedBreakerA,
            interruptingCapacityPasses = breakerInterruptingRatingKA >= availableFaultCurrentKA
        )
    }

    fun suggestedNextStandardRating(designCurrentA: Double): Double? {
        require(designCurrentA.isFinite() && designCurrentA > 0.0)
        return commonBreakerRatingsA.firstOrNull { it >= designCurrentA }
    }
}
