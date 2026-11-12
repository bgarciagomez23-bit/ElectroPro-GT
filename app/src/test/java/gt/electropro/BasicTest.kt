package gt.electropro

import org.junit.Assert.assertEquals
import org.junit.Test

class BasicTest {
    @Test fun threePhaseCurrentExample() {
        val power = 50_000.0
        val voltage = 208.0
        val powerFactor = 0.90
        val current = power / (kotlin.math.sqrt(3.0) * voltage * powerFactor)
        assertEquals(154.35, current, 0.02)
    }
}
