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
import gt.electropro.domain.calculations.AmpacityChecker
import java.util.Locale

@Composable
fun AmpacityScreen(onBack: () -> Unit) {
    var nonContinuous by remember { mutableStateOf("20") }
    var continuous by remember { mutableStateOf("40") }
    var multiplier by remember { mutableStateOf("1.25") }
    var tableAmpacity by remember { mutableStateOf("100") }
    var ambientFactor by remember { mutableStateOf("1.0") }
    var adjustmentFactor by remember { mutableStateOf("1.0") }
    var terminalAmpacity by remember { mutableStateOf("75") }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<gt.electropro.domain.calculations.AmpacityCheckResult?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("ELECTROPRO GT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Verificación de ampacidad", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Ingresa la ampacidad de tabla y los factores aplicables según la edición NEC adoptada y las condiciones reales.", fontSize = 12.sp)
        AmpacityField(nonContinuous, { nonContinuous = it; error = null }, "Carga no continua (A)")
        AmpacityField(continuous, { continuous = it; error = null }, "Carga continua (A)")
        AmpacityField(multiplier, { multiplier = it; error = null }, "Multiplicador de carga continua")
        AmpacityField(tableAmpacity, { tableAmpacity = it; error = null }, "Ampacidad base de tabla (A)")
        AmpacityField(ambientFactor, { ambientFactor = it; error = null }, "Factor de corrección por temperatura")
        AmpacityField(adjustmentFactor, { adjustmentFactor = it; error = null }, "Factor de ajuste por agrupamiento")
        AmpacityField(terminalAmpacity, { terminalAmpacity = it; error = null }, "Ampacidad admisible por terminales (A)")
        Button(onClick = {
            try {
                result = AmpacityChecker.calculate(
                    nonContinuous.toDouble(), continuous.toDouble(), multiplier.toDouble(),
                    tableAmpacity.toDouble(), ambientFactor.toDouble(), adjustmentFactor.toDouble(),
                    terminalAmpacity.toDouble()
                )
                error = null
            } catch (e: Exception) {
                result = null
                error = e.message ?: "Verifica los datos."
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Verificar conductor") }
        error?.let { Text("Dato inválido: $it", color = MaterialTheme.colorScheme.error) }
        result?.let {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("RESULTADOS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("Ampacidad requerida: ${fmtAmp(it.requiredAmpacityA)} A")
                    Text("Ampacidad corregida: ${fmtAmp(it.correctedConductorAmpacityA)} A")
                    Text("Límite admisible de terminales: ${fmtAmp(it.terminalAllowableAmpacityA)} A")
                    Text(if (it.conductorPasses) "Conductor: cumple con los datos ingresados" else "Conductor: ampacidad insuficiente",
                        color = if (it.conductorPasses) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    Text(if (it.terminalPasses) "Terminales: cumple con el límite ingresado" else "Terminales: límite insuficiente",
                        color = if (it.terminalPasses) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }
            }
        }
        Text("IMPORTANTE: esta herramienta no contiene tablas NEC. La ampacidad base, factores y límite de terminales deben verificarse en la edición normativa aplicable, tipo de aislamiento, material, calibre, temperatura de terminales, ambiente y cantidad de conductores portadores de corriente. No constituye certificación de cumplimiento.", fontSize = 11.sp)
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}

@Composable
private fun AmpacityField(value: String, onValue: (String) -> Unit, label: String) {
    OutlinedTextField(value, onValue, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
}

private fun fmtAmp(value: Double) = String.format(Locale.US, "%.2f", value)
