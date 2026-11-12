package gt.electropro.domain.calculations

data class GroundGridStudyInputs(
    val faultCurrentA: Double,
    val clearingTimeSeconds: Double,
    val decrementFactor: Double,
    val currentDivisionFactor: Double,
    val soilResistivityOhmM: Double,
    val surfaceLayerResistivityOhmM: Double,
    val surfaceLayerThicknessM: Double,
    val burialDepthM: Double,
    val bodyWeightKg: Int,
    val gridLengthXM: Double,
    val gridLengthYM: Double,
    val conductorSpacingXM: Double,
    val conductorSpacingYM: Double,
    val electrodeLengthM: Double,
    val electrodeCount: Int
)

data class GroundGridStudyValidation(
    val errors: List<String>,
    val warnings: List<String>
) {
    val isStructurallyValid: Boolean get() = errors.isEmpty()
}

object GroundGridStudyValidator {
    /**
     * Input completeness/range checks only. This deliberately does not claim
     * IEEE Std 80 compliance or calculate permissible touch/step voltages.
     */
    fun validate(input: GroundGridStudyInputs): GroundGridStudyValidation {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        fun positive(value: Double, label: String) {
            if (!value.isFinite() || value <= 0.0) errors += "$label debe ser mayor que cero."
        }
        positive(input.faultCurrentA, "La corriente de falla")
        positive(input.clearingTimeSeconds, "El tiempo de despeje")
        positive(input.decrementFactor, "El factor de decremento")
        positive(input.currentDivisionFactor, "El factor de división de corriente")
        positive(input.soilResistivityOhmM, "La resistividad del suelo")
        positive(input.surfaceLayerResistivityOhmM, "La resistividad de la capa superficial")
        positive(input.surfaceLayerThicknessM, "El espesor de la capa superficial")
        if (!input.burialDepthM.isFinite() || input.burialDepthM < 0.0) errors += "La profundidad de enterramiento no puede ser negativa."
        positive(input.gridLengthXM, "La dimensión X")
        positive(input.gridLengthYM, "La dimensión Y")
        positive(input.conductorSpacingXM, "La separación X")
        positive(input.conductorSpacingYM, "La separación Y")
        positive(input.electrodeLengthM, "La longitud de electrodo")
        if (input.electrodeCount < 0) errors += "La cantidad de electrodos no puede ser negativa."
        if (input.bodyWeightKg !in listOf(50, 70)) errors += "Selecciona un peso corporal de 50 kg o 70 kg."
        if (input.decrementFactor < 1.0) warnings += "Verifica el factor de decremento y su fuente; se ingresó un valor menor que 1."
        if (input.currentDivisionFactor > 1.0) warnings += "Verifica el factor de división de corriente; se ingresó un valor mayor que 1."
        if (input.surfaceLayerResistivityOhmM < input.soilResistivityOhmM) {
            warnings += "La resistividad superficial es menor que la del suelo; confirma mediciones y modelo de capas."
        }
        return GroundGridStudyValidation(errors, warnings)
    }
}
