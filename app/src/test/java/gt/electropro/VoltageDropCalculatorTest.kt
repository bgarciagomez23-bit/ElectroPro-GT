package gt.electropro

import gt.electropro.domain.calculations.ConductorMaterial
import gt.electropro.domain.calculations.SystemPhase
import gt.electropro.domain.calculations.VoltageDropCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class VoltageDropCalculatorTest {
    @Test
    fun singlePhaseCopperVoltageDrop() {
        val result = VoltageDropCalculator.calculate(
            currentA = 20.0,
            oneWayLengthFt = 100.0,
            systemVoltageV = 120.0,
            conductorAreaCircularMils = 41_740.0,
            material = ConductorMaterial.COPPER_75C,
            phase = SystemPhase.SINGLE_PHASE
        )
        assertEquals(1.236, result.voltageDropV, 0.01)
        assertEquals(1.03, result.voltageDropPercent, 0.02)
        assertEquals(118.764, result.receivingVoltageV, 0.02)
    }

    @Test
    fun threePhaseAluminumVoltageDrop() {
        val result = VoltageDropCalculator.calculate(
            currentA = 50.0,
            oneWayLengthFt = 150.0,
            systemVoltageV = 480.0,
            conductorAreaCircularMils = 105_600.0,
            material = ConductorMaterial.ALUMINUM_75C,
            phase = SystemPhase.THREE_PHASE
        )
        assertEquals(2.258, result.voltageDropV, 0.02)
    }

    @Test
    fun rejectsZeroConductorArea() {
        assertThrows(IllegalArgumentException::class.java) {
            VoltageDropCalculator.calculate(
                20.0, 100.0, 120.0, 0.0,
                ConductorMaterial.COPPER_75C, SystemPhase.SINGLE_PHASE
            )
        }
    }
}
