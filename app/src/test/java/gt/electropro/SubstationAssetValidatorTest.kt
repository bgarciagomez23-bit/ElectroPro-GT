package gt.electropro

import gt.electropro.domain.calculations.SubstationAsset
import gt.electropro.domain.calculations.SubstationAssetType
import gt.electropro.domain.calculations.SubstationAssetValidator
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class SubstationAssetValidatorTest {
    private fun asset() = SubstationAsset(
        tag = "TR-01",
        name = "Transformador principal",
        type = SubstationAssetType.TRANSFORMER,
        ratedVoltageV = 13800.0,
        ratedCurrentA = 31.38,
        ratedPowerKva = 750.0,
        shortCircuitRatingKA = null,
        location = "Subestación principal",
        notes = "Datos de placa"
    )

    @Test
    fun acceptsEquipmentWithValidRequiredData() {
        assertTrue(SubstationAssetValidator.validate(asset()).isEmpty())
    }

    @Test
    fun rejectsMissingTagAndInvalidVoltage() {
        val errors = SubstationAssetValidator.validate(asset().copy(tag = "", ratedVoltageV = 0.0))
        assertFalse(errors.isEmpty())
        assertTrue(errors.size >= 2)
    }

    @Test
    fun allowsOptionalRatingsToBeUnspecified() {
        assertTrue(SubstationAssetValidator.validate(asset().copy(ratedCurrentA = null, ratedPowerKva = null)).isEmpty())
    }
}
