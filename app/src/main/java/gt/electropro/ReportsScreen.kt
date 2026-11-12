package gt.electropro

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gt.electropro.data.local.ElectroProDatabase
import gt.electropro.data.local.InspectionRecordEntity
import gt.electropro.data.local.ThermographyRecordEntity
import gt.electropro.data.local.ElectricalTestRecordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun ReportsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { ElectroProDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    var pdfBytes by remember { mutableStateOf<ByteArray?>(null) }
    var csvBytes by remember { mutableStateOf<ByteArray?>(null) }
    var message by remember { mutableStateOf("Los informes reúnen inspecciones, termografía y pruebas eléctricas guardadas en este dispositivo.") }
    var generating by remember { mutableStateOf(false) }

    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val bytes = pdfBytes
        if (uri != null && bytes != null) {
            runCatching {
                val stream = requireNotNull(context.contentResolver.openOutputStream(uri)) { "No se pudo abrir el destino PDF." }
                stream.use { it.write(bytes); it.flush() }
            }.onSuccess { message = "Informe PDF exportado." }
                .onFailure { message = "No fue posible guardar el PDF: ${it.localizedMessage ?: "error de escritura"}" }
        }
    }
    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        val bytes = csvBytes
        if (uri != null && bytes != null) {
            runCatching {
                val stream = requireNotNull(context.contentResolver.openOutputStream(uri)) { "No se pudo abrir el destino CSV." }
                stream.use { it.write(bytes); it.flush() }
            }.onSuccess { message = "Archivo CSV compatible con Excel exportado." }
                .onFailure { message = "No fue posible guardar el CSV: ${it.localizedMessage ?: "error de escritura"}" }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("INFORMES", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Exportación de reportes", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Genera un resumen consolidado de los registros locales. El CSV puede abrirse en Excel; no es un libro XLSX nativo.", fontSize = 12.sp)
        Card {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Contenido incluido", fontWeight = FontWeight.Bold)
                Text("• Inspecciones: fecha, área, hallazgo, severidad, estado y acción correctiva")
                Text("• Termografía: equipo, fase, temperaturas, carga y observaciones")
                Text("• Pruebas: método, tensión, duración, lectura, unidad y estado registrado")
                Text("• Se indica si cada registro tiene evidencia fotográfica adjunta")
            }
        }
        Button(enabled = !generating, onClick = {
            generating = true
            scope.launch {
                try {
                    val inspections = db.inspectionRecordDao().observeAll().first()
                    val thermography = db.thermographyRecordDao().observeAll().first()
                    val tests = db.electricalTestRecordDao().observeAll().first()
                    pdfBytes = buildReportPdf(inspections, thermography, tests)
                    csvBytes = buildReportCsv(inspections, thermography, tests)
                    message = "Informe preparado: ${inspections.size} inspecciones, ${thermography.size} mediciones y ${tests.size} pruebas."
                } catch (e: Exception) {
                    message = "No se pudo preparar el informe. Verifica el almacenamiento local."
                } finally { generating = false }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text(if (generating) "Preparando..." else "Generar informe consolidado") }
        Button(enabled = pdfBytes != null && !generating, onClick = { pdfLauncher.launch("ElectroProGT_Informe.pdf") }, modifier = Modifier.fillMaxWidth()) {
            Text("Guardar informe PDF")
        }
        OutlinedButton(enabled = csvBytes != null && !generating, onClick = { csvLauncher.launch("ElectroProGT_Registros.csv") }, modifier = Modifier.fillMaxWidth()) {
            Text("Exportar datos para Excel (CSV)")
        }
        Text(message, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        Text("Nota: las exportaciones contienen los datos y la referencia a la existencia de fotografías; las imágenes no se incrustan todavía en el PDF. El estado de una prueba es el registrado por el usuario, no una certificación automática de conformidad.", fontSize = 11.sp)
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}

private fun buildReportCsv(
    inspections: List<InspectionRecordEntity>,
    thermography: List<ThermographyRecordEntity>,
    tests: List<ElectricalTestRecordEntity>
): ByteArray {
    fun cell(value: Any?): String = "\"" + (value?.toString() ?: "").replace("\"", "\"\"") + "\""
    val rows = mutableListOf<List<String>>()
    rows += listOf("TIPO", "FECHA", "PROYECTO", "EQUIPO_O_AREA", "DETALLE", "VALOR", "UNIDAD", "SEVERIDAD_O_ESTADO", "OBSERVACIONES", "FOTOGRAFIA")
    inspections.forEach { r -> rows += listOf("INSPECCION",r.inspectionDate,r.projectCode,r.systemArea,r.finding,"","", "${r.severity} / ${r.status}",r.correctiveAction,if(r.photoUri != null) "ADJUNTA" else "NO") }
    thermography.forEach { r -> rows += listOf("TERMOGRAFIA",r.inspectionDate,"",r.equipmentTag,"${r.location} · Fase ${r.phase}",r.measuredTemperatureC.toString(),"°C",r.severity,r.finding,if(r.photoUri != null) "ADJUNTA" else "NO") }
    tests.forEach { r -> rows += listOf("PRUEBA",r.testDate,r.projectCode,r.equipmentTag,r.testType,r.measuredValue.toString(),r.unit,r.result,r.notes,if(r.photoUri != null) "ADJUNTA" else "NO") }
    val text = rows.joinToString("\r\n") { row -> row.joinToString(",") { cell(it) } }
    return ("\uFEFF" + text).toByteArray(Charsets.UTF_8)
}

private fun buildReportPdf(
    inspections: List<InspectionRecordEntity>,
    thermography: List<ThermographyRecordEntity>,
    tests: List<ElectricalTestRecordEntity>
): ByteArray {
    val document = PdfDocument()
    val output = ByteArrayOutputStream()
    val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true }
    val headingPaint = Paint().apply { textSize = 12f; isFakeBoldText = true }
    val bodyPaint = Paint().apply { textSize = 9f }
    val smallPaint = Paint().apply { textSize = 8f }
    val pageWidth = 595
    val pageHeight = 842
    var pageNumber = 0
    var page: PdfDocument.Page? = null
    var y = 48f

    fun newPage() {
        page?.let { document.finishPage(it) }
        pageNumber++
        page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth,pageHeight,pageNumber).create())
        y = 42f
        val canvas = page!!.canvas
        canvas.drawText("ELECTROPRO GT · INFORME DE CAMPO", 36f, y, headingPaint)
        canvas.drawText("Página $pageNumber", 500f, y, smallPaint)
        y += 30f
    }
    fun line(text: String, paint: Paint = bodyPaint, indent: Float = 36f) {
        if (y > 790f) newPage()
        val maxChars = if (paint.textSize >= 12f) 68 else 100
        text.chunked(maxChars).forEach { part ->
            if (y > 790f) newPage()
            page!!.canvas.drawText(part, indent, y, paint)
            y += if (paint.textSize >= 12f) 18f else 13f
        }
    }

    newPage()
    line("INFORME CONSOLIDADO", titlePaint)
    line("Registros locales de inspección, termografía y pruebas eléctricas")
    line("Resumen: ${inspections.size} inspecciones · ${thermography.size} mediciones · ${tests.size} pruebas")
    y += 10f
    line("1. INSPECCIONES", headingPaint)
    if (inspections.isEmpty()) line("Sin registros.")
    inspections.forEach { r ->
        line("${r.inspectionDate} | ${r.projectCode} | ${r.systemArea} | ${r.severity} / ${r.status}")
        line("Hallazgo: ${r.finding}")
        if (r.correctiveAction.isNotBlank()) line("Acción: ${r.correctiveAction}")
        line("Inspector: ${r.inspector} | Fotografía: ${if(r.photoUri != null) "Adjunta" else "No adjunta"}", smallPaint)
        y += 5f
    }
    line("2. TERMOGRAFÍA", headingPaint)
    if (thermography.isEmpty()) line("Sin registros.")
    thermography.forEach { r ->
        line("${r.inspectionDate} | ${r.equipmentTag} | ${r.location} | Fase ${r.phase}")
        line("Medida: ${r.measuredTemperatureC} °C | Referencia: ${r.referenceTemperatureC ?: "N/D"} °C | Ambiente: ${r.ambientTemperatureC ?: "N/D"} °C")
        line("Carga: ${r.loadPercent?.let { "$it %" } ?: "N/D"} | Severidad registrada: ${r.severity}")
        if (r.finding.isNotBlank()) line("Observación: ${r.finding}")
        line("Termograma: ${if(r.photoUri != null) "Adjunto" else "No adjunto"}", smallPaint)
        y += 5f
    }
    line("3. PRUEBAS ELÉCTRICAS", headingPaint)
    if (tests.isEmpty()) line("Sin registros.")
    tests.forEach { r ->
        line("${r.testDate} | ${r.equipmentTag} | ${r.testType}")
        line("Lectura: ${r.measuredValue} ${r.unit} | Tensión: ${r.testVoltageV ?: "N/D"} V | Duración: ${r.durationSeconds ?: "N/D"} s")
        line("Estado registrado: ${r.result} | Ambiente: ${r.ambientTemperatureC ?: "N/D"} °C")
        if (r.notes.isNotBlank()) line("Notas: ${r.notes}")
        line("Fotografía: ${if(r.photoUri != null) "Adjunta" else "No adjunta"}", smallPaint)
        y += 5f
    }
    y += 12f
    line("NOTA TÉCNICA", headingPaint)
    line("Este documento consolida datos ingresados por el usuario. No sustituye protocolos,")
    line("criterios de aceptación ni revisión de un profesional responsable.")
    document.finishPage(page!!)
    document.writeTo(output)
    document.close()
    return output.toByteArray()
}
