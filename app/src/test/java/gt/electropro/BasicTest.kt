package gt.electropro

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.sqrt

class BasicTest {
    @Test fun threePhaseCurrentExample() {
        val power = 50_000.0
        val voltage = 208.0
        val powerFactor = 0.90
        val current = power / (sqrt(3.0) * voltage * powerFactor)
        // I = P / (sqrt(3) * V_LL * FP) = 154.21 A
        assertEquals(154.21, current, 0.02)
    }
}
