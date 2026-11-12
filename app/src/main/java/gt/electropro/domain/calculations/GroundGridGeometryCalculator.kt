package gt.electropro.domain.calculations

import kotlin.math.floor

data class GroundGridGeometryResult(
    val conductorsParallelToX: Int,
    val conductorsParallelToY: Int,
    val totalHorizontalConductorLengthM: Double,
    val perimeterConductorLengthM: Double,
    val rodCount: Int,
    val totalRodLengthM: Double,
    val totalBuriedConductorLengthM: Double,
    val intersectionCount: Int
)

object GroundGridGeometryCalculator {
    /**
     * Geometric quantity takeoff only. Does not calculate IEEE 80 safety limits,
     * grid resistance, GPR, touch/step voltages, or current distribution.
     * Conductors are assumed uniformly spaced with an additional conductor at each boundary.
     */
    fun calculate(
        lengthXM: Double,
        lengthYM: Double,
        spacingXM: Double,
        spacingYM: Double,
        rodCount: Int,
        rodLengthM: Double
    ): GroundGridGeometryResult {
        require(lengthXM.isFinite() && lengthXM > 0.0) { "La dimensión X debe ser mayor que cero." }
        require(lengthYM.isFinite() && lengthYM > 0.0) { "La dimensión Y debe ser mayor que cero." }
        require(spacingXM.isFinite() && spacingXM > 0.0) { "El espaciamiento X debe ser mayor que cero." }
        require(spacingYM.isFinite() && spacingYM > 0.0) { "El espaciamiento Y debe ser mayor que cero." }
        require(rodCount >= 0) { "La cantidad de varillas no puede ser negativa." }
        require(rodLengthM.isFinite() && rodLengthM >= 0.0) { "La longitud de varilla no puede ser negativa." }

        val parallelToX = floor(lengthYM / spacingYM).toInt() + 1
        val parallelToY = floor(lengthXM / spacingXM).toInt() + 1
        val horizontal = parallelToX * lengthXM + parallelToY * lengthYM
        val perimeter = 2.0 * (lengthXM + lengthYM)
        val rods = rodCount * rodLengthM
        return GroundGridGeometryResult(
            conductorsParallelToX = parallelToX,
            conductorsParallelToY = parallelToY,
            totalHorizontalConductorLengthM = horizontal,
            perimeterConductorLengthM = perimeter,
            rodCount = rodCount,
            totalRodLengthM = rods,
            totalBuriedConductorLengthM = horizontal + rods,
            intersectionCount = parallelToX * parallelToY
        )
    }
}
