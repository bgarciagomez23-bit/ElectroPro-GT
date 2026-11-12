package gt.electropro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gt.electropro.domain.calculations.ConductorMaterial
import gt.electropro.domain.calculations.SystemPhase
import gt.electropro.domain.calculations.VoltageDropCalculator
import java.util.Locale

@Composable
fun VoltageDropScreen(onBack: () -> Unit) {
    var phase by remember { mutableStateOf(SystemPhase.THREE_PHASE) }
    var material by remember { mutableStateOf(ConductorMaterial.COPPER_75C) }
    var current by remember { mutableStateOf("50") }
    var length by remember { mutableStateOf("100") }
    var voltage by remember { mutableStateOf("208") }
    var area by remember { mutableStateOf("41740") }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<gt.electropro.domain.calculations.VoltageDropResult?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("ELECTROPRO GT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Caída de tensión", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Estimación resistiva. Longitud de recorrido en un solo sentido; área en circular mils (CM).", fontSize = 12.sp)
        Row {
            FilterChip(selected = phase == SystemPhase.SINGLE_PHASE, onClick = { phase = SystemPhase.SINGLE_PHASE }, label = { Text("Monofásico") })
            Spacer(Modifier.width(8.dp))
            FilterChip(selected = phase == SystemPhase.THREE_PHASE, onClick = { phase = SystemPhase.THREE_PHASE }, label = { Text("Trifásico") })
        }
        Row {
            FilterChip(selected = material == ConductorMaterial.COPPER_75C, onClick = { material = ConductorMaterial.COPPER_75C }, label = { Text("Cobre 75 °C") })
            Spacer(Modifier.width(8.dp))
            FilterChip(selected = material == ConductorMaterial.ALUMINUM_75C, onClick = { material = ConductorMaterial.ALUMINUM_75C }, label = { Text("Aluminio 75 °C") })
        }
        NumericField(current, { current = it; error = null }, "Corriente (A)")
        NumericField(length, { length = it; error = null }, "Longitud unidireccional (ft)")
        NumericField(voltage, { voltage = it; error = null }, "Tensión del sistema (V)")
        NumericField(area, { area = it; error = null }, "Área del conductor (CM)")
        Button(onClick = {
            try {
                result = VoltageDropCalculator.calculate(
                    current.toDouble(), length.toDouble(), voltage.toDouble(), area.toDouble(), material, phase
                )
                error = null
            } catch (e: Exception) {
                result = null
                error = e.message ?: "Verifica los datos."
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Calcular caída de tensión") }
        error?.let { Text("Dato inválido: $it", color = MaterialTheme.colorScheme.error) }
        result?.let {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("RESULTADOS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("Caída: ${fmt(it.voltageDropV)} V")
                    Text("Caída porcentual: ${fmt(it.voltageDropPercent)} %")
                    Text("Tensión estimada al extremo: ${fmt(it.receivingVoltageV)} V")
                    Text("K aplicado: ${material.kOhmCircularMilsPerFoot} Ω·CM/ft")
                }
            }
        }
        Text("Advertencia: no incluye reactancia, temperatura real, agrupamiento ni correcciones de ampacidad. No constituye verificación de cumplimiento NEC.", fontSize = 11.sp)
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}

@Composable
private fun NumericField(value: String, onValue: (String) -> Unit, label: String) {
    OutlinedTextField(value, onValue, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
}

private fun fmt(value: Double) = String.format(Locale.US, "%.2f", value)
