package gt.electropro

import gt.electropro.domain.calculations.TransformerCalculator
import gt.electropro.domain.calculations.TransformerPhase
import org.junit.Assert.assertEquals
import org.junit.Test

class TransformerCalculatorTest {
    @Test
    fun calculatesThreePhaseTransformerCurrentsAndFaultEstimate() {
        val result = TransformerCalculator.calculate(
            ratedPowerKva = 750.0,
            primaryVoltageV = 13200.0,
            secondaryVoltageV = 208.0,
            impedancePercent = 5.75,
            loadKva = 600.0,
            phase = TransformerPhase.THREE_PHASE
        )
        assertEquals(32.805, result.primaryCurrentA, 0.01)
        assertEquals(2081.38, result.secondaryCurrentA, 0.1)
        assertEquals(80.0, result.loadPercent, 0.0001)
        assertEquals(36.197, result.secondaryFaultCurrentKA, 0.01)
    }

    @Test
    fun calculatesSinglePhaseCurrent() {
        val result = TransformerCalculator.calculate(100.0, 13200.0, 240.0, 4.0, 50.0, TransformerPhase.SINGLE_PHASE)
        assertEquals(7.5758, result.primaryCurrentA, 0.001)
        assertEquals(416.6667, result.secondaryCurrentA, 0.001)
        assertEquals(10.4167, result.secondaryFaultCurrentKA, 0.001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsZeroImpedance() {
        TransformerCalculator.calculate(100.0, 13200.0, 240.0, 0.0, 50.0, TransformerPhase.SINGLE_PHASE)
    }
}
