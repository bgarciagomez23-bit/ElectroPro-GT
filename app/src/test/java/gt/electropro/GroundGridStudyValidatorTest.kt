package gt.electropro

import gt.electropro.domain.calculations.GroundGridStudyInputs
import gt.electropro.domain.calculations.GroundGridStudyValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GroundGridStudyValidatorTest {
    private fun validInputs() = GroundGridStudyInputs(
        faultCurrentA = 12000.0,
        clearingTimeSeconds = 0.5,
        decrementFactor = 1.1,
        currentDivisionFactor = 0.7,
        soilResistivityOhmM = 80.0,
        surfaceLayerResistivityOhmM = 2500.0,
        surfaceLayerThicknessM = 0.1,
        burialDepthM = 0.6,
        bodyWeightKg = 70,
        gridLengthXM = 20.0,
        gridLengthYM = 15.0,
        conductorSpacingXM = 5.0,
        conductorSpacingYM = 5.0,
        electrodeLengthM = 3.0,
        electrodeCount = 8
    )

    @Test
    fun acceptsCompletePositiveStudyInputs() {
        assertTrue(GroundGridStudyValidator.validate(validInputs()).isStructurallyValid)
    }

    @Test
    fun rejectsInvalidCurrentAndBodyWeight() {
        val input = validInputs().copy(faultCurrentA = 0.0, bodyWeightKg = 60)
        val result = GroundGridStudyValidator.validate(input)
        assertFalse(result.isStructurallyValid)
        assertTrue(result.errors.size >= 2)
    }

    @Test
    fun flagsUnusualFactorsWithoutSilentlyChangingThem() {
        val input = validInputs().copy(decrementFactor = 0.8, currentDivisionFactor = 1.2)
        val result = GroundGridStudyValidator.validate(input)
        assertTrue(result.isStructurallyValid)
        assertEquals(2, result.warnings.size)
    }
}
