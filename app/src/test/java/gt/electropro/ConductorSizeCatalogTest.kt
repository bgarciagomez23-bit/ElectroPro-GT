package gt.electropro

import gt.electropro.domain.calculations.ConductorSizeCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConductorSizeCatalogTest {
    @Test
    fun containsCommonCopperAndAluminumSizeAreas() {
        assertEquals(41_740.0, ConductorSizeCatalog.sizes.first { it.designation == "4 AWG" }.circularMils, 0.0)
        assertEquals(211_600.0, ConductorSizeCatalog.sizes.first { it.designation == "4/0 AWG" }.circularMils, 0.0)
        assertEquals(500_000.0, ConductorSizeCatalog.sizes.first { it.designation == "500 kcmil" }.circularMils, 0.0)
    }

    @Test
    fun catalogIsOrderedByIncreasingArea() {
        assertTrue(ConductorSizeCatalog.sizes.zipWithNext().all { it.first.circularMils < it.second.circularMils })
    }
}
