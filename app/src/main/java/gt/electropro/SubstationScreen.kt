package gt.electropro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gt.electropro.data.local.ElectroProDatabase
import gt.electropro.data.local.SubstationAssetEntity
import gt.electropro.domain.calculations.SubstationAsset
import gt.electropro.domain.calculations.SubstationAssetType
import gt.electropro.domain.calculations.SubstationAssetValidator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val dao = remember { ElectroProDatabase.getInstance(context).substationAssetDao() }
    val scope = rememberCoroutineScope()
    val assets by dao.observeAll().collectAsState(initial = emptyList())

    var tag by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var voltage by remember { mutableStateOf("") }
    var current by remember { mutableStateOf("") }
    var power by remember { mutableStateOf("") }
    var shortCircuit by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(SubstationAssetType.TRANSFORMER) }
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("ELECTROPRO GT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Inventario de subestación", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Inventario local persistente: los equipos se conservan al cerrar la aplicación.", fontSize = 12.sp)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(value = type.label, onValueChange = {}, readOnly = true, label = { Text("Tipo de equipo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor())
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                SubstationAssetType.values().forEach { option ->
                    DropdownMenuItem(text = { Text(option.label) }, onClick = { type = option; expanded = false })
                }
            }
        }
        SubstationField(tag, { tag = it; error = null }, "Código / tag")
        SubstationField(name, { name = it; error = null }, "Nombre del equipo")
        SubstationField(voltage, { voltage = it; error = null }, "Tensión nominal (V)")
        SubstationField(current, { current = it; error = null }, "Corriente nominal (A), opcional")
        SubstationField(power, { power = it; error = null }, "Potencia nominal (kVA), opcional")
        SubstationField(shortCircuit, { shortCircuit = it; error = null }, "Capacidad cortocircuito (kA), opcional")
        SubstationField(location, { location = it }, "Ubicación")
        SubstationField(notes, { notes = it }, "Observaciones")
        Button(enabled = !saving, onClick = {
            try {
                val asset = SubstationAsset(
                    tag = tag.trim(), name = name.trim(), type = type,
                    ratedVoltageV = voltage.toDouble(),
                    ratedCurrentA = current.toOptionalDouble(),
                    ratedPowerKva = power.toOptionalDouble(),
                    shortCircuitRatingKA = shortCircuit.toOptionalDouble(),
                    location = location.trim(), notes = notes.trim()
                )
                val errors = SubstationAssetValidator.validate(asset)
                if (errors.isNotEmpty()) {
                    error = errors.joinToString("\n")
                } else {
                    saving = true
                    scope.launch {
                        try {
                            if (dao.countByTag(asset.tag) > 0) {
                                error = "Ya existe un equipo con ese código/tag."
                            } else {
                                dao.insert(
                                    SubstationAssetEntity(
                                        tag = asset.tag,
                                        name = asset.name,
                                        type = asset.type.name,
                                        ratedVoltageV = asset.ratedVoltageV,
                                        ratedCurrentA = asset.ratedCurrentA,
                                        ratedPowerKva = asset.ratedPowerKva,
                                        shortCircuitRatingKA = asset.shortCircuitRatingKA,
                                        location = asset.location,
                                        notes = asset.notes
                                    )
                                )
                                tag = ""; name = ""; voltage = ""; current = ""; power = ""; shortCircuit = ""
                                location = ""; notes = ""; error = null
                            }
                        } catch (e: Exception) {
                            error = "No se pudo guardar el equipo. Verifica que el código sea único."
                        } finally {
                            saving = false
                        }
                    }
                }
            } catch (e: Exception) {
                error = "Revisa la tensión y los valores numéricos ingresados."
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text(if (saving) "Guardando..." else "Guardar equipo")
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
        HorizontalDivider()
        Text("Equipos registrados: ${assets.size}", fontWeight = FontWeight.SemiBold)
        assets.forEach { asset ->
            Card {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("${asset.tag} · ${asset.name}", fontWeight = FontWeight.SemiBold)
                    Text("${SubstationAssetType.valueOf(asset.type).label} | ${asset.ratedVoltageV} V")
                    Text("In: ${asset.ratedCurrentA?.let { "${it} A" } ?: "No indicado"} · Potencia: ${asset.ratedPowerKva?.let { "${it} kVA" } ?: "No indicada"}")
                    asset.shortCircuitRatingKA?.let { Text("Capacidad de cortocircuito: ${it} kA") }
                    if (asset.location.isNotBlank()) Text("Ubicación: ${asset.location}")
                    if (asset.notes.isNotBlank()) Text("Notas: ${asset.notes}")
                    TextButton(onClick = { scope.launch { dao.deleteById(asset.id) } }) {
                        Text("Eliminar registro", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        Text("IMPORTANTE: el inventario no verifica coordinación, capacidad interruptiva, aislamiento, niveles BIL, compatibilidad de barras ni cumplimiento normativo. Confirma los datos contra placas, planos y pruebas de campo. La base de datos es local en este dispositivo; la sincronización en nube y la asociación formal a proyectos se implementarán después.", fontSize = 11.sp)
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}

@Composable
private fun SubstationField(value: String, onValue: (String) -> Unit, label: String) {
    OutlinedTextField(value, onValue, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
}

private fun String.toOptionalDouble(): Double? = trim().takeIf { it.isNotEmpty() }?.toDouble()
