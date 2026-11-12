package gt.electropro

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gt.electropro.data.local.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { ElectroProDatabase.getInstance(context) }
    val dao = db.inspectionRecordDao()
    val scope = rememberCoroutineScope()
    val records by dao.observeAll().collectAsState(initial = emptyList())
    var project by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var inspector by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var finding by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("MEDIA") }
    var status by remember { mutableStateOf("PENDIENTE") }
    var action by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            photoUri = uri.toString()
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("INSPECCIONES ELÉCTRICAS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Registro de inspección", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Registra hallazgos de campo, severidad, acción correctiva y evidencia fotográfica.", fontSize = 12.sp)
        FieldInput(project, { project = it }, "Código de proyecto")
        FieldInput(date, { date = it }, "Fecha (AAAA-MM-DD)")
        FieldInput(inspector, { inspector = it }, "Inspector responsable")
        FieldInput(area, { area = it }, "Área / tablero / sistema")
        FieldInput(finding, { finding = it }, "Hallazgo / condición observada", singleLine = false)
        FieldInput(action, { action = it }, "Acción correctiva / recomendación", singleLine = false)
        FieldChoice("Severidad", severity, listOf("BAJA", "MEDIA", "ALTA", "CRÍTICA")) { severity = it }
        FieldChoice("Estado", status, listOf("PENDIENTE", "EN PROCESO", "CORREGIDO", "VERIFICADO")) { status = it }
        OutlinedButton(onClick = { photoPicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text(if (photoUri == null) "Adjuntar fotografía" else "Fotografía adjunta · Cambiar")
        }
        if (photoUri != null) Text("Evidencia seleccionada y vinculada al registro.", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
        Button(enabled = !saving, onClick = {
            if (date.isBlank() || inspector.isBlank() || area.isBlank() || finding.isBlank()) message = "Completa fecha, inspector, área y hallazgo."
            else {
                saving = true
                scope.launch {
                    try {
                        dao.insert(InspectionRecordEntity(projectCode=project.trim(), inspectionDate=date.trim(), inspector=inspector.trim(), systemArea=area.trim(), finding=finding.trim(), severity=severity, status=status, correctiveAction=action.trim(), photoUri=photoUri))
                        project=""; date=""; inspector=""; area=""; finding=""; action=""; severity="MEDIA"; status="PENDIENTE"; photoUri=null
                        message="Inspección guardada correctamente."
                    } catch (e: Exception) { message="No se pudo guardar la inspección." }
                    finally { saving=false }
                }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text(if (saving) "Guardando..." else "Guardar inspección") }
        message?.let { Text(it, color=MaterialTheme.colorScheme.primary, fontSize=12.sp) }
        HorizontalDivider()
        Text("HISTORIAL · ${records.size}", fontWeight = FontWeight.Bold)
        records.forEach { record ->
            Card {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${record.inspectionDate} · ${record.systemArea}", fontWeight = FontWeight.Bold)
                    Text("Proyecto: ${record.projectCode.ifBlank { "Sin código" }} · Inspector: ${record.inspector}")
                    Text("Severidad: ${record.severity} · Estado: ${record.status}")
                    Text(record.finding)
                    if (record.correctiveAction.isNotBlank()) Text("Acción: ${record.correctiveAction}")
                    if (record.photoUri != null) Text("Con evidencia fotográfica", color=MaterialTheme.colorScheme.primary, fontSize=11.sp)
                    TextButton(onClick={scope.launch { dao.deleteById(record.id) }}) { Text("Eliminar registro", color=MaterialTheme.colorScheme.error) }
                }
            }
        }
        OutlinedButton(onClick=onBack, modifier=Modifier.fillMaxWidth()) { Text("Volver") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermographyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { ElectroProDatabase.getInstance(context) }
    val dao = db.thermographyRecordDao()
    val scope = rememberCoroutineScope()
    val records by dao.observeAll().collectAsState(initial = emptyList())
    var date by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phase by remember { mutableStateOf("A") }
    var measured by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var ambient by remember { mutableStateOf("") }
    var load by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("MEDIA") }
    var finding by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            photoUri = uri.toString()
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("TERMOGRAFÍA", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Registro termográfico", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Registra temperaturas medidas, condiciones de carga y evidencia infrarroja. No clasifica automáticamente el riesgo.", fontSize = 12.sp)
        FieldInput(date, { date=it }, "Fecha (AAAA-MM-DD)")
        FieldInput(tag, { tag=it }, "Equipo / tablero / breaker")
        FieldInput(location, { location=it }, "Ubicación / componente")
        FieldChoice("Fase", phase, listOf("A", "B", "C", "N", "AB", "BC", "CA")) { phase=it }
        FieldInput(measured, { measured=it }, "Temperatura medida (°C)", KeyboardType.Decimal)
        FieldInput(reference, { reference=it }, "Temperatura de referencia (°C), opcional", KeyboardType.Decimal)
        FieldInput(ambient, { ambient=it }, "Temperatura ambiente (°C), opcional", KeyboardType.Decimal)
        FieldInput(load, { load=it }, "Carga durante medición (%) opcional", KeyboardType.Decimal)
        FieldChoice("Clasificación del hallazgo", severity, listOf("BAJA", "MEDIA", "ALTA", "CRÍTICA")) { severity=it }
        FieldInput(finding, { finding=it }, "Observación / diferencial / recomendación", singleLine=false)
        OutlinedButton(onClick={photoPicker.launch(arrayOf("image/*"))}, modifier=Modifier.fillMaxWidth()) {
            Text(if(photoUri==null) "Adjuntar termograma" else "Termograma adjunto · Cambiar")
        }
        Button(enabled=!saving, onClick={
            val t=measured.toDoubleOrNull()
            val ref=reference.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
            val amb=ambient.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
            val pct=load.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
            if(date.isBlank() || tag.isBlank() || location.isBlank() || t==null || !t.isFinite()) message="Completa fecha, equipo, ubicación y temperatura válida."
            else if((reference.isNotBlank() && (ref==null || !ref.isFinite())) || (ambient.isNotBlank() && (amb==null || !amb.isFinite())) || (load.isNotBlank() && (pct==null || !pct.isFinite() || pct<0.0 || pct>100.0))) message="Verifica las temperaturas y el porcentaje de carga."
            else {
                saving=true
                scope.launch {
                    try {
                        dao.insert(ThermographyRecordEntity(inspectionDate=date.trim(), equipmentTag=tag.trim(), location=location.trim(), phase=phase, measuredTemperatureC=t, referenceTemperatureC=ref, ambientTemperatureC=amb, loadPercent=pct, severity=severity, finding=finding.trim(), photoUri=photoUri))
                        date=""; tag=""; location=""; phase="A"; measured=""; reference=""; ambient=""; load=""; severity="MEDIA"; finding=""; photoUri=null
                        message="Medición termográfica guardada."
                    } catch(e:Exception) { message="No se pudo guardar la medición." }
                    finally { saving=false }
                }
            }
        }, modifier=Modifier.fillMaxWidth()) { Text(if(saving) "Guardando..." else "Guardar medición") }
        message?.let { Text(it, color=MaterialTheme.colorScheme.primary, fontSize=12.sp) }
        HorizontalDivider()
        Text("HISTORIAL · ${records.size}", fontWeight=FontWeight.Bold)
        records.forEach { record ->
            Card {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Text("${record.inspectionDate} · ${record.equipmentTag}", fontWeight=FontWeight.Bold)
                    Text("${record.location} · Fase ${record.phase}")
                    Text("Temperatura: ${record.measuredTemperatureC} °C · Severidad: ${record.severity}")
                    record.referenceTemperatureC?.let { Text("Referencia registrada: $it °C") }
                    record.ambientTemperatureC?.let { Text("Ambiente: $it °C") }
                    record.loadPercent?.let { Text("Carga: $it %") }
                    if(record.finding.isNotBlank()) Text(record.finding)
                    if(record.photoUri!=null) Text("Termograma adjunto", color=MaterialTheme.colorScheme.primary, fontSize=11.sp)
                    TextButton(onClick={scope.launch { dao.deleteById(record.id) }}) { Text("Eliminar registro", color=MaterialTheme.colorScheme.error) }
                }
            }
        }
        Text("La temperatura por sí sola no determina la severidad. Evalúa diferencial térmico, carga, emisividad, material, ambiente, conexiones equivalentes y criterios del procedimiento aplicable.", fontSize=11.sp)
        OutlinedButton(onClick=onBack, modifier=Modifier.fillMaxWidth()) { Text("Volver") }
    }
}

@Composable
private fun FieldInput(
    value: String,
    onValue: (String) -> Unit,
    label: String,
    keyboard: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value=value, onValueChange=onValue, modifier=Modifier.fillMaxWidth(),
        label={Text(label)}, singleLine=singleLine,
        keyboardOptions=KeyboardOptions(keyboardType=keyboard)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FieldChoice(label: String, value: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded=expanded, onExpandedChange={expanded=it}) {
        OutlinedTextField(
            value=value, onValueChange={}, readOnly=true, label={Text(label)},
            trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(expanded)},
            modifier=Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded=expanded, onDismissRequest={expanded=false}) {
            options.forEach { option -> DropdownMenuItem(text={Text(option)}, onClick={onSelected(option); expanded=false}) }
        }
    }
}
