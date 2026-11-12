package gt.electropro

import gt.electropro.domain.calculations.GroundingConductorChecker
import gt.electropro.domain.calculations.SoilResistivityCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GroundingCalculatorTest {
    @Test
    fun calculatesWennerResistivity() {
        val result = SoilResistivityCalculator.wenner(1.0, 10.0)
        assertEquals(62.831853, result.apparentResistivityOhmM, 0.00001)
    }

    @Test
    fun appliesAdiabaticConductorEquation() {
        val result = GroundingConductorChecker.adiabatic(10000.0, 1.0, 226.0, 50.0)
        assertEquals(44.2478, result.minimumAreaMm2, 0.001)
        assertTrue(result.passes)
    }
}
