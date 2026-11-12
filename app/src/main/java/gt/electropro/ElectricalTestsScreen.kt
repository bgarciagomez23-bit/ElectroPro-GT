package gt.electropro

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import gt.electropro.data.local.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectricalTestsScreen(onBack: () -> Unit) {
    val context=LocalContext.current
    val dao=remember { ElectroProDatabase.getInstance(context).electricalTestRecordDao() }
    val scope=rememberCoroutineScope()
    val records by dao.observeAll().collectAsState(initial=emptyList())
    var project by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("") }
    var testType by remember { mutableStateOf("RESISTENCIA DE AISLAMIENTO") }
    var voltage by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var measured by remember { mutableStateOf("") }
    var ambient by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("REGISTRADO") }
    var notes by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if(uri!=null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            photo=uri.toString()
        }
    }
    val unit=when(testType) {
        "RESISTENCIA DE AISLAMIENTO" -> "MΩ"
        "CONTINUIDAD" -> "Ω"
        "RESISTENCIA DE TIERRA" -> "Ω"
        else -> "mA"
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement=Arrangement.spacedBy(9.dp)) {
        Text("PRUEBAS ELÉCTRICAS", color=MaterialTheme.colorScheme.primary, fontWeight=FontWeight.Bold)
        Text("Registro de ensayos", fontSize=23.sp, fontWeight=FontWeight.Bold)
        Text("Registra método, parámetros de ensayo, lectura, condiciones y evidencia. La app no declara conformidad automática.", fontSize=12.sp)
        TestField(project,{project=it},"Código de proyecto")
        TestField(date,{date=it},"Fecha (AAAA-MM-DD)")
        TestField(tag,{tag=it},"Equipo / circuito / conductor")
        TestChoice("Tipo de prueba",testType,listOf("RESISTENCIA DE AISLAMIENTO","CONTINUIDAD","RESISTENCIA DE TIERRA","CORRIENTE DE FUGA")) { testType=it }
        TestField(voltage,{voltage=it},"Tensión de prueba (V DC), si aplica",KeyboardType.Decimal)
        TestField(duration,{duration=it},"Duración (s), si aplica",KeyboardType.Decimal)
        TestField(measured,{measured=it},"Lectura medida ($unit)",KeyboardType.Decimal)
        TestField(ambient,{ambient=it},"Temperatura ambiente (°C), opcional",KeyboardType.Decimal)
        TestChoice("Estado del registro",result,listOf("REGISTRADO","APROBADO POR RESPONSABLE","REQUIERE REVISIÓN","NO CONFORME")) { result=it }
        TestField(notes,{notes=it},"Puntos de prueba, instrumento, calibración, condiciones y observaciones",singleLine=false)
        OutlinedButton(onClick={picker.launch(arrayOf("image/*"))},modifier=Modifier.fillMaxWidth()) { Text(if(photo==null) "Adjuntar fotografía del ensayo" else "Evidencia adjunta · Cambiar") }
        Button(enabled=!saving,onClick={
            val reading=measured.toDoubleOrNull()
            val testV=voltage.trim().takeIf{it.isNotEmpty()}?.toDoubleOrNull()
            val seconds=duration.trim().takeIf{it.isNotEmpty()}?.toDoubleOrNull()
            val temp=ambient.trim().takeIf{it.isNotEmpty()}?.toDoubleOrNull()
            if(date.isBlank()||tag.isBlank()||reading==null||!reading.isFinite()||reading<0.0) message="Completa fecha, equipo y lectura válida (no negativa)."
            else if((voltage.isNotBlank()&&(testV==null||!testV.isFinite()||testV<=0.0))||(duration.isNotBlank()&&(seconds==null||!seconds.isFinite()||seconds<=0.0))||(ambient.isNotBlank()&&(temp==null||!temp.isFinite()))) message="Verifica tensión, duración y temperatura."
            else {
                saving=true
                scope.launch {
                    try {
                        dao.insert(ElectricalTestRecordEntity(projectCode=project.trim(),testDate=date.trim(),equipmentTag=tag.trim(),testType=testType,testVoltageV=testV,durationSeconds=seconds,measuredValue=reading,unit=unit,ambientTemperatureC=temp,result=result,notes=notes.trim(),photoUri=photo))
                        project="";date="";tag="";voltage="";duration="";measured="";ambient="";notes="";photo=null;result="REGISTRADO"
                        message="Resultado de prueba guardado."
                    } catch(e:Exception) { message="No se pudo guardar el resultado." }
                    finally { saving=false }
                }
            }
        },modifier=Modifier.fillMaxWidth()) { Text(if(saving) "Guardando..." else "Guardar resultado") }
        message?.let { Text(it,color=MaterialTheme.colorScheme.primary,fontSize=12.sp) }
        HorizontalDivider()
        Text("HISTORIAL · ${records.size}",fontWeight=FontWeight.Bold)
        records.forEach { record ->
            Card {
                Column(Modifier.fillMaxWidth().padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Text("${record.testDate} · ${record.equipmentTag}",fontWeight=FontWeight.Bold)
                    Text(record.testType)
                    Text("Lectura: ${record.measuredValue} ${record.unit} · ${record.result}")
                    record.testVoltageV?.let { Text("Tensión de ensayo: $it V") }
                    record.durationSeconds?.let { Text("Duración: $it s") }
                    record.ambientTemperatureC?.let { Text("Ambiente: $it °C") }
                    if(record.notes.isNotBlank()) Text(record.notes)
                    if(record.photoUri!=null) Text("Evidencia adjunta",color=MaterialTheme.colorScheme.primary,fontSize=11.sp)
                    TextButton(onClick={scope.launch { dao.deleteById(record.id) }}) { Text("Eliminar",color=MaterialTheme.colorScheme.error) }
                }
            }
        }
        Text("Referencia técnica: seleccionar el procedimiento y los criterios de aceptación de la edición normativa aplicable (NEC, IEEE, IEC o ANSI/NETA). Registrar tensión, tiempo, temperatura, instrumento y condiciones. No se fija un límite universal ni se declara aprobación automática.",fontSize=11.sp)
        OutlinedButton(onClick=onBack,modifier=Modifier.fillMaxWidth()) { Text("Volver") }
    }
}

@Composable
private fun TestField(value:String,onValue:(String)->Unit,label:String,keyboard:KeyboardType=KeyboardType.Text,singleLine:Boolean=true) {
    OutlinedTextField(value=value,onValueChange=onValue,modifier=Modifier.fillMaxWidth(),label={Text(label)},singleLine=singleLine,keyboardOptions=KeyboardOptions(keyboardType=keyboard))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestChoice(label:String,value:String,options:List<String>,onSelected:(String)->Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded=expanded,onExpandedChange={expanded=it}) {
        OutlinedTextField(value=value,onValueChange={},readOnly=true,label={Text(label)},trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(expanded)},modifier=Modifier.fillMaxWidth().menuAnchor())
        ExposedDropdownMenu(expanded=expanded,onDismissRequest={expanded=false}) {
            options.forEach { option -> DropdownMenuItem(text={Text(option)},onClick={onSelected(option);expanded=false}) }
        }
    }
}
