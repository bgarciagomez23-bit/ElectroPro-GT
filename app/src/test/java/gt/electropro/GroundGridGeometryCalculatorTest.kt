package gt.electropro

import gt.electropro.domain.calculations.GroundGridGeometryCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class GroundGridGeometryCalculatorTest {
    @Test
    fun calculatesRectangularGridConductorQuantities() {
        val result = GroundGridGeometryCalculator.calculate(20.0, 10.0, 5.0, 5.0, 4, 3.0)
        assertEquals(3, result.conductorsParallelToX)
        assertEquals(5, result.conductorsParallelToY)
        assertEquals(110.0, result.totalHorizontalConductorLengthM, 0.0001)
        assertEquals(60.0, result.perimeterConductorLengthM, 0.0001)
        assertEquals(12.0, result.totalRodLengthM, 0.0001)
        assertEquals(122.0, result.totalBuriedConductorLengthM, 0.0001)
        assertEquals(15, result.intersectionCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsZeroSpacing() {
        GroundGridGeometryCalculator.calculate(20.0, 10.0, 0.0, 5.0, 0, 0.0)
    }
}
