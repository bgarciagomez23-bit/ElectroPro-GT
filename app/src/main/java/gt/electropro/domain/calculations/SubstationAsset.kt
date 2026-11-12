package gt.electropro.domain.calculations

enum class SubstationAssetType(val label: String) {
    TRANSFORMER("Transformador"),
    MV_SWITCHGEAR("Celda de media tensión"),
    LV_SWITCHBOARD("Tablero de baja tensión"),
    FEEDER("Alimentador"),
    GENERATOR("Planta eléctrica"),
    CAPACITOR_BANK("Banco de capacitores")
}

data class SubstationAsset(
    val tag: String,
    val name: String,
    val type: SubstationAssetType,
    val ratedVoltageV: Double,
    val ratedCurrentA: Double?,
    val ratedPowerKva: Double?,
    val shortCircuitRatingKA: Double?,
    val location: String,
    val notes: String
)

object SubstationAssetValidator {
    fun validate(asset: SubstationAsset): List<String> {
        val errors = mutableListOf<String>()
        if (asset.tag.isBlank()) errors += "El código o tag es obligatorio."
        if (asset.name.isBlank()) errors += "El nombre del equipo es obligatorio."
        if (!asset.ratedVoltageV.isFinite() || asset.ratedVoltageV <= 0.0) errors += "La tensión nominal debe ser mayor que cero."
        if (asset.ratedCurrentA != null && (!asset.ratedCurrentA.isFinite() || asset.ratedCurrentA <= 0.0)) errors += "La corriente nominal debe ser positiva."
        if (asset.ratedPowerKva != null && (!asset.ratedPowerKva.isFinite() || asset.ratedPowerKva <= 0.0)) errors += "La potencia nominal debe ser positiva."
        if (asset.shortCircuitRatingKA != null && (!asset.shortCircuitRatingKA.isFinite() || asset.shortCircuitRatingKA <= 0.0)) errors += "La capacidad de cortocircuito debe ser positiva."
        return errors
    }
}
