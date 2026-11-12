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
import gt.electropro.domain.calculations.GroundingConductorChecker
import gt.electropro.domain.calculations.SoilResistivityCalculator
import java.util.Locale

@Composable
fun GroundingScreen(onBack: () -> Unit) {
    var spacing by remember { mutableStateOf("1") }
    var resistance by remember { mutableStateOf("10") }
    var depth by remember { mutableStateOf("0") }
    var fault by remember { mutableStateOf("10000") }
    var time by remember { mutableStateOf("1") }
    var k by remember { mutableStateOf("226") }
    var area by remember { mutableStateOf("50") }
    var resistivity by remember { mutableStateOf<Double?>(null) }
    var conductorResult by remember { mutableStateOf<gt.electropro.domain.calculations.GroundingConductorResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("ELECTROPRO GT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Puesta a tierra", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Resistividad aparente por método Wenner", fontWeight = FontWeight.SemiBold)
        GroundField(spacing, { spacing = it }, "Separación entre picas a (m)")
        GroundField(resistance, { resistance = it }, "Resistencia medida R (Ω)")
        GroundField(depth, { depth = it }, "Profundidad de picas (m)")
        Button(onClick = {
            try {
                resistivity = SoilResistivityCalculator.wenner(spacing.toDouble(), resistance.toDouble(), depth.toDouble()).apparentResistivityOhmM
                error = null
            } catch (e: Exception) { resistivity = null; error = e.message ?: "Revisa los datos." }
        }, modifier = Modifier.fillMaxWidth()) { Text("Calcular resistividad") }
        resistivity?.let { Text("Resistividad aparente: ${groundFmt(it)} Ω·m") }

        HorizontalDivider()
        Text("Verificación térmica preliminar del conductor", fontWeight = FontWeight.SemiBold)
        GroundField(fault, { fault = it }, "Corriente de falla (A)")
        GroundField(time, { time = it }, "Tiempo de despeje (s)")
        GroundField(k, { k = it }, "Factor k (A·√s/mm²)")
        GroundField(area, { area = it }, "Área seleccionada (mm²)")
        Button(onClick = {
            try {
                conductorResult = GroundingConductorChecker.adiabatic(fault.toDouble(), time.toDouble(), k.toDouble(), area.toDouble())
                error = null
            } catch (e: Exception) { conductorResult = null; error = e.message ?: "Revisa los datos." }
        }, modifier = Modifier.fillMaxWidth()) { Text("Verificar sección térmica") }
        conductorResult?.let {
            Text("Sección mínima calculada: ${groundFmt(it.minimumAreaMm2)} mm²")
            Text(if (it.passes) "Sección seleccionada supera el mínimo térmico ingresado." else "Sección seleccionada insuficiente para los datos ingresados.",
                color = if (it.passes) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text("IMPORTANTE: resistividad Wenner es aparente para el espaciamiento medido y no determina por sí sola el modelo estratificado del suelo. La ecuación adiabática requiere k validado para material y temperaturas; esto no calcula malla IEEE 80, tensiones de paso/contacto, GPR, geometría, reparto de corriente ni cumplimiento normativo. Requiere estudio de ingeniería y datos de campo.", fontSize = 11.sp)
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}

@Composable
private fun GroundField(value: String, onValue: (String) -> Unit, label: String) {
    OutlinedTextField(value, onValue, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
}
private fun groundFmt(value: Double) = String.format(Locale.US, "%.2f", value)
