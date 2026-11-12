package gt.electropro

import gt.electropro.domain.calculations.ProtectionChecker
import org.junit.Assert.*
import org.junit.Test

class ProtectionCheckerTest {
    @Test
    fun checksLoadConductorAndInterruptingCapacity() {
        val result = ProtectionChecker.calculate(20.0, 40.0, 1.25, 80.0, 90.0, 10.0, 22.0)
        assertEquals(70.0, result.designCurrentA, 0.001)
        assertTrue(result.loadCurrentPasses)
        assertTrue(result.conductorCoordinationPasses)
        assertTrue(result.interruptingCapacityPasses)
        assertTrue(result.overallPasses)
    }

    @Test
    fun detectsInsufficientInterruptingRating() {
        val result = ProtectionChecker.calculate(10.0, 20.0, 1.25, 50.0, 60.0, 25.0, 10.0)
        assertFalse(result.interruptingCapacityPasses)
        assertFalse(result.overallPasses)
    }

    @Test
    fun suggestsNextStandardRating() {
        assertEquals(80.0, ProtectionChecker.suggestedNextStandardRating(71.0)!!, 0.0)
        assertNull(ProtectionChecker.suggestedNextStandardRating(5000.0))
    }
}
