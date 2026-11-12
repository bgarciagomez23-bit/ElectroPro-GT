package gt.electropro

import gt.electropro.domain.calculations.ElectricalCalculator
import gt.electropro.domain.calculations.SystemPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ElectricalCalculatorTest {
    @Test
    fun threePhaseCurrentFromActivePower() {
        val current = ElectricalCalculator.currentFromActivePower(
            activePowerW = 50_000.0,
            voltageV = 208.0,
            powerFactor = 0.90,
            phase = SystemPhase.THREE_PHASE
        )
        assertEquals(154.21, current, 0.02)
    }

    @Test
    fun singlePhaseCurrentFromActivePower() {
        val current = ElectricalCalculator.currentFromActivePower(
            activePowerW = 10_000.0,
            voltageV = 230.0,
            powerFactor = 0.95,
            phase = SystemPhase.SINGLE_PHASE
        )
        assertEquals(45.77, current, 0.02)
    }

    @Test
    fun threePhasePowerQuantitiesAreConsistent() {
        val result = ElectricalCalculator.powerFromVoltageCurrent(
            voltageV = 208.0,
            currentA = 154.21,
            powerFactor = 0.90,
            phase = SystemPhase.THREE_PHASE
        )
        assertEquals(49_999.0, result.activePowerW, 15.0)
        assertEquals(55_554.0, result.apparentPowerVA, 20.0)
        assertEquals(24_200.0, result.reactivePowerVar, 30.0)
    }

    @Test
    fun rejectsZeroPowerFactor() {
        assertThrows(IllegalArgumentException::class.java) {
            ElectricalCalculator.currentFromActivePower(
                1_000.0, 120.0, 0.0, SystemPhase.SINGLE_PHASE
            )
        }
    }

    @Test
    fun rejectsNegativeVoltage() {
        assertThrows(IllegalArgumentException::class.java) {
            ElectricalCalculator.currentFromActivePower(
                1_000.0, -120.0, 0.9, SystemPhase.SINGLE_PHASE
            )
        }
    }
}
