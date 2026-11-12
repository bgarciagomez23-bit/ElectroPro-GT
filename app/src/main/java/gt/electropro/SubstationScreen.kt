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
    val database = remember { ElectroProDatabase.getInstance(context) }
    val dao = database.substationAssetDao()
    val substations by database.substationDao().observeAll().collectAsState(initial = emptyList())
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
    var substationExpanded by remember { mutableStateOf(false) }
    var selectedSubstationId by remember { mutableStateOf<Long?>(null) }
    var filterSubstationId by remember { mutableStateOf<Long?>(null) }
    var filterExpanded by remember { mutableStateOf(false) }
    val visibleAssets = assets.filter { filterSubstationId == null || it.substationId == filterSubstationId }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var editingAssetId by remember { mutableStateOf<Long?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("ELECTROPRO GT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Inventario de subestación", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Inventario local persistente: los equipos se conservan al cerrar la aplicación.", fontSize = 12.sp)
        ExposedDropdownMenuBox(expanded = substationExpanded, onExpandedChange = { substationExpanded = it }) {
            OutlinedTextField(
                value = substations.firstOrNull { it.id == selectedSubstationId }?.let { "${it.code} · ${it.name}" } ?: "Sin subestación asignada",
                onValueChange = {}, readOnly = true, label = { Text("Subestación del equipo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(substationExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = substationExpanded, onDismissRequest = { substationExpanded = false }) {
                DropdownMenuItem(text = { Text("Sin subestación asignada") }, onClick = { selectedSubstationId = null; substationExpanded = false })
                substations.forEach { station ->
                    DropdownMenuItem(text = { Text("${station.code} · ${station.name}") }, onClick = { selectedSubstationId = station.id; substationExpanded = false })
                }
            }
        }
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
                            val currentEditId = editingAssetId
                            val duplicateCount = if (currentEditId == null) dao.countByTag(asset.tag)
                                else dao.countByTagExceptId(asset.tag, currentEditId)
                            if (duplicateCount > 0) {
                                error = "Ya existe otro equipo con ese código/tag."
                            } else {
                                val existing = assets.firstOrNull { it.id == currentEditId }
                                val record = SubstationAssetEntity(
                                    id = currentEditId ?: 0L,
                                    tag = asset.tag,
                                    name = asset.name,
                                    type = asset.type.name,
                                    ratedVoltageV = asset.ratedVoltageV,
                                    ratedCurrentA = asset.ratedCurrentA,
                                    ratedPowerKva = asset.ratedPowerKva,
                                    shortCircuitRatingKA = asset.shortCircuitRatingKA,
                                    location = asset.location,
                                    notes = asset.notes,
                                    createdAtEpochMs = existing?.createdAtEpochMs ?: System.currentTimeMillis(),
                                    substationId = selectedSubstationId
                                )
                                if (currentEditId == null) dao.insert(record) else dao.update(record)
                                tag = ""; name = ""; voltage = ""; current = ""; power = ""; shortCircuit = ""
                                location = ""; notes = ""; selectedSubstationId = null
                                type = SubstationAssetType.TRANSFORMER
                                editingAssetId = null
                                error = null
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
            Text(if (saving) "Guardando..." else if (editingAssetId != null) "Actualizar equipo" else "Guardar equipo")
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
        HorizontalDivider()
        Text("FILTRAR INVENTARIO", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        ExposedDropdownMenuBox(expanded = filterExpanded, onExpandedChange = { filterExpanded = it }) {
            OutlinedTextField(
                value = if (filterSubstationId == null) "Todas las subestaciones" else substations.firstOrNull { it.id == filterSubstationId }?.let { "${it.code} · ${it.name}" } ?: "Todas las subestaciones",
                onValueChange = {}, readOnly = true, label = { Text("Mostrar equipos de") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(filterExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
                DropdownMenuItem(text = { Text("Todas las subestaciones") }, onClick = { filterSubstationId = null; filterExpanded = false })
                substations.forEach { station ->
                    DropdownMenuItem(text = { Text("${station.code} · ${station.name}") }, onClick = { filterSubstationId = station.id; filterExpanded = false })
                }
                DropdownMenuItem(text = { Text("Sin subestación asignada") }, onClick = { filterSubstationId = -1L; filterExpanded = false })
            }
        }
        val filteredAssets = if (filterSubstationId == -1L) assets.filter { it.substationId == null } else visibleAssets
        Text("Equipos mostrados: ${filteredAssets.size} de ${assets.size}", fontWeight = FontWeight.SemiBold)
        filteredAssets.forEach { asset ->
            Card {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("${asset.tag} · ${asset.name}", fontWeight = FontWeight.SemiBold)
                    Text("${SubstationAssetType.valueOf(asset.type).label} | ${asset.ratedVoltageV} V")
                    Text("Subestación: ${substations.firstOrNull { it.id == asset.substationId }?.name ?: "Sin asignar"}")
                    Text("In: ${asset.ratedCurrentA?.let { "${it} A" } ?: "No indicado"} · Potencia: ${asset.ratedPowerKva?.let { "${it} kVA" } ?: "No indicada"}")
                    asset.shortCircuitRatingKA?.let { Text("Capacidad de cortocircuito: ${it} kA") }
                    if (asset.location.isNotBlank()) Text("Ubicación: ${asset.location}")
                    if (asset.notes.isNotBlank()) Text("Notas: ${asset.notes}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            editingAssetId = asset.id
                            tag = asset.tag
                            name = asset.name
                            type = runCatching { SubstationAssetType.valueOf(asset.type) }.getOrDefault(SubstationAssetType.TRANSFORMER)
                            voltage = asset.ratedVoltageV.toString()
                            current = asset.ratedCurrentA?.toString() ?: ""
                            power = asset.ratedPowerKva?.toString() ?: ""
                            shortCircuit = asset.shortCircuitRatingKA?.toString() ?: ""
                            location = asset.location
                            notes = asset.notes
                            selectedSubstationId = asset.substationId
                            error = null
                        }) { Text("Editar") }
                        TextButton(onClick = { scope.launch { dao.deleteById(asset.id) } }) {
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
        Text("IMPORTANTE: el inventario no verifica coordinación, capacidad interruptiva, aislamiento, niveles BIL, compatibilidad de barras ni cumplimiento normativo. Confirma los datos contra placas, planos y pruebas de campo. La base de datos es local en este dispositivo; la sincronización en nube aún no está habilitada.", fontSize = 11.sp)
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}

@Composable
private fun SubstationField(value: String, onValue: (String) -> Unit, label: String) {
    OutlinedTextField(value, onValue, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
}

private fun String.toOptionalDouble(): Double? = trim().takeIf { it.isNotEmpty() }?.toDouble()
