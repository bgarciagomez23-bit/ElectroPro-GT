package gt.electropro

import gt.electropro.domain.calculations.AmpacityChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class AmpacityCheckerTest {
    @Test
    fun appliesLoadAndCorrectionFactors() {
        val result = AmpacityChecker.calculate(
            nonContinuousLoadA = 20.0,
            continuousLoadA = 40.0,
            continuousLoadMultiplier = 1.25,
            tableAmpacityA = 100.0,
            ambientCorrectionFactor = 0.91,
            adjustmentFactor = 0.8,
            terminalAllowableAmpacityA = 75.0
        )
        assertEquals(70.0, result.requiredAmpacityA, 0.001)
        assertEquals(72.8, result.correctedConductorAmpacityA, 0.001)
        assertTrue(result.conductorPasses)
        assertTrue(result.terminalPasses)
        assertTrue(result.overallPasses)
    }

    @Test
    fun detectsInsufficientCorrectedAmpacity() {
        val result = AmpacityChecker.calculate(10.0, 60.0, 1.25, 100.0, 0.8, 0.8, 100.0)
        assertFalse(result.conductorPasses)
        assertFalse(result.overallPasses)
    }

    @Test
    fun rejectsInvalidCorrectionFactor() {
        assertThrows(IllegalArgumentException::class.java) {
            AmpacityChecker.calculate(10.0, 20.0, 1.25, 100.0, 1.2, 1.0, 100.0)
        }
    }
}
