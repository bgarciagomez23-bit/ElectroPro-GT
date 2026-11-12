package gt.electropro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gt.electropro.data.local.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectManagementScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { ElectroProDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    val projects by db.projectDao().observeAll().collectAsState(initial = emptyList())
    val substations by db.substationDao().observeAll().collectAsState(initial = emptyList())
    val assets by db.substationAssetDao().observeAll().collectAsState(initial = emptyList())

    var projectCode by remember { mutableStateOf("") }
    var projectName by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var projectLocation by remember { mutableStateOf("") }
    var projectNotes by remember { mutableStateOf("") }
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }
    var projectMenu by remember { mutableStateOf(false) }
    var subCode by remember { mutableStateOf("") }
    var subName by remember { mutableStateOf("") }
    var subVoltage by remember { mutableStateOf("") }
    var subLocation by remember { mutableStateOf("") }
    var subNotes by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("ELECTROPRO GT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Proyectos y subestaciones", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("REGISTRAR PROYECTO", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        OutlinedTextField(projectCode, { projectCode = it.uppercase(); message = null }, Modifier.fillMaxWidth(), label = { Text("Código del proyecto") }, singleLine = true)
        OutlinedTextField(projectName, { projectName = it; message = null }, Modifier.fillMaxWidth(), label = { Text("Nombre del proyecto") }, singleLine = true)
        OutlinedTextField(client, { client = it }, Modifier.fillMaxWidth(), label = { Text("Cliente") }, singleLine = true)
        OutlinedTextField(projectLocation, { projectLocation = it }, Modifier.fillMaxWidth(), label = { Text("Ubicación") }, singleLine = true)
        OutlinedTextField(projectNotes, { projectNotes = it }, Modifier.fillMaxWidth(), label = { Text("Observaciones") })
        Button(enabled = !saving, onClick = {
            if (projectCode.isBlank() || projectName.isBlank()) message = "Código y nombre del proyecto son obligatorios."
            else {
                saving = true
                scope.launch {
                    try {
                        if (db.projectDao().countByCode(projectCode.trim()) > 0) message = "El código del proyecto ya existe."
                        else {
                            db.projectDao().insert(ProjectEntity(code=projectCode.trim(), name=projectName.trim(), client=client.trim(), location=projectLocation.trim(), notes=projectNotes.trim()))
                            projectCode=""; projectName=""; client=""; projectLocation=""; projectNotes=""
                            message="Proyecto guardado correctamente."
                        }
                    } catch (e: Exception) { message="No se pudo guardar el proyecto." }
                    finally { saving=false }
                }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Guardar proyecto") }

        HorizontalDivider()
        Text("REGISTRAR SUBESTACIÓN", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        ExposedDropdownMenuBox(expanded=projectMenu, onExpandedChange={projectMenu=it}) {
            OutlinedTextField(
                value=projects.firstOrNull { it.id == selectedProjectId }?.let { "${it.code} · ${it.name}" } ?: "Sin proyecto asociado",
                onValueChange={}, readOnly=true, label={Text("Proyecto asociado (opcional)")},
                trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(projectMenu)},
                modifier=Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded=projectMenu, onDismissRequest={projectMenu=false}) {
                DropdownMenuItem(text={Text("Sin proyecto asociado")}, onClick={selectedProjectId=null; projectMenu=false})
                projects.forEach { p -> DropdownMenuItem(text={Text("${p.code} · ${p.name}")}, onClick={selectedProjectId=p.id; projectMenu=false}) }
            }
        }
        OutlinedTextField(subCode, {subCode=it.uppercase(); message=null}, Modifier.fillMaxWidth(), label={Text("Código de subestación")}, singleLine=true)
        OutlinedTextField(subName, {subName=it}, Modifier.fillMaxWidth(), label={Text("Nombre de subestación")}, singleLine=true)
        OutlinedTextField(subVoltage, {subVoltage=it}, Modifier.fillMaxWidth(), label={Text("Tensión nominal (V), opcional")}, singleLine=true)
        OutlinedTextField(subLocation, {subLocation=it}, Modifier.fillMaxWidth(), label={Text("Ubicación")}, singleLine=true)
        OutlinedTextField(subNotes, {subNotes=it}, Modifier.fillMaxWidth(), label={Text("Observaciones")})
        Button(enabled=!saving, onClick={
            val v = subVoltage.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
            if(subCode.isBlank() || subName.isBlank()) message="Código y nombre de subestación son obligatorios."
            else if(subVoltage.isNotBlank() && (v == null || v <= 0.0)) message="La tensión debe ser un número mayor que cero."
            else {
                saving=true
                scope.launch {
                    try {
                        if(db.substationDao().countByCode(subCode.trim())>0) message="El código de subestación ya existe."
                        else {
                            db.substationDao().insert(SubstationEntity(projectId=selectedProjectId, code=subCode.trim(), name=subName.trim(), nominalVoltageV=v, location=subLocation.trim(), notes=subNotes.trim()))
                            subCode=""; subName=""; subVoltage=""; subLocation=""; subNotes=""
                            message="Subestación guardada correctamente."
                        }
                    } catch(e:Exception) { message="No se pudo guardar la subestación." }
                    finally { saving=false }
                }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Guardar subestación") }
        message?.let { Text(it, color=if(it.contains("correctamente")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, fontSize=12.sp) }
        HorizontalDivider()
        Text("PROYECTOS REGISTRADOS: ${projects.size}", fontWeight=FontWeight.SemiBold)
        projects.forEach { p ->
            Card {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("${p.code} · ${p.name}", fontWeight=FontWeight.Bold)
                    if(p.client.isNotBlank()) Text("Cliente: ${p.client}")
                    if(p.location.isNotBlank()) Text("Ubicación: ${p.location}")
                    Text("Subestaciones: ${substations.count { it.projectId == p.id }}")
                    TextButton(onClick={scope.launch { db.projectDao().deleteById(p.id) }}) { Text("Eliminar proyecto (conserva subestaciones sin asociación)", color=MaterialTheme.colorScheme.error) }
                }
            }
        }
        Text("SUBESTACIONES REGISTRADAS: ${substations.size}", fontWeight=FontWeight.SemiBold)
        substations.forEach { s ->
            Card {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("${s.code} · ${s.name}", fontWeight=FontWeight.Bold)
                    Text("Proyecto: ${projects.firstOrNull { it.id == s.projectId }?.name ?: "Sin proyecto"}")
                    s.nominalVoltageV?.let { Text("Tensión nominal: $it V") }
                    if(s.location.isNotBlank()) Text("Ubicación: ${s.location}")
                    Text("Equipos: ${assets.count { it.substationId == s.id }}")
                    TextButton(onClick={scope.launch { db.substationDao().deleteById(s.id) }}) { Text("Eliminar subestación (conserva equipos sin asociación)", color=MaterialTheme.colorScheme.error) }
                }
            }
        }
        OutlinedButton(onClick=onBack, Modifier.fillMaxWidth()) { Text("Volver") }
    }
}
