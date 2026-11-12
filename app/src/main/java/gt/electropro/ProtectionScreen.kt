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
import gt.electropro.domain.calculations.ProtectionChecker
import java.util.Locale

@Composable
fun ProtectionScreen(onBack: () -> Unit) {
    var nonContinuous by remember { mutableStateOf("20") }
    var continuous by remember { mutableStateOf("40") }
    var multiplier by remember { mutableStateOf("1.25") }
    var breaker by remember { mutableStateOf("80") }
    var conductor by remember { mutableStateOf("90") }
    var fault by remember { mutableStateOf("10") }
    var interrupting by remember { mutableStateOf("22") }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<gt.electropro.domain.calculations.ProtectionCheckResult?>(null) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("ELECTROPRO GT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Verificación de protecciones", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Comprobación preliminar de carga, conductor y capacidad interruptiva.", fontSize = 12.sp)
        ProtectionField(nonContinuous, { nonContinuous = it; error = null }, "Carga no continua (A)")
        ProtectionField(continuous, { continuous = it; error = null }, "Carga continua (A)")
        ProtectionField(multiplier, { multiplier = it; error = null }, "Multiplicador de carga continua")
        ProtectionField(breaker, { breaker = it; error = null }, "Corriente nominal del breaker (A)")
        ProtectionField(conductor, { conductor = it; error = null }, "Ampacidad admisible del conductor (A)")
        ProtectionField(fault, { fault = it; error = null }, "Corriente de falla disponible (kA)")
        ProtectionField(interrupting, { interrupting = it; error = null }, "Capacidad interruptiva del breaker (kA)")
        Button(onClick = {
            try {
                result = ProtectionChecker.calculate(
                    nonContinuous.toDouble(), continuous.toDouble(), multiplier.toDouble(),
                    breaker.toDouble(), conductor.toDouble(), fault.toDouble(), interrupting.toDouble()
                )
                error = null
            } catch (e: Exception) {
                result = null
                error = e.message ?: "Verifica los datos."
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Verificar protección") }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        result?.let {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("RESULTADOS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("Corriente de diseño: ${protectionFmt(it.designCurrentA)} A")
                    Text("Breaker seleccionado: ${protectionFmt(it.selectedBreakerA)} A")
                    Text("Siguiente tamaño estándar sugerido: ${ProtectionChecker.suggestedNextStandardRating(it.designCurrentA)?.let { rating -> protectionFmt(rating) + " A" } ?: "Fuera del catálogo"}")
                    ProtectionStatus("Breaker frente a carga", it.loadCurrentPasses)
                    ProtectionStatus("Conductor frente a breaker", it.conductorCoordinationPasses)
                    ProtectionStatus("Capacidad interruptiva", it.interruptingCapacityPasses)
                }
            }
        }
        Text("ADVERTENCIA: no sustituye el estudio de cortocircuito, coordinación/selectividad ni la revisión de excepciones y reglas aplicables de NEC (por ejemplo, motores, transformadores, protección de conductores y ajustes normalizados). Verifica tensión, polos, curva, SCCR/AIC, condiciones de instalación y edición normativa.", fontSize = 11.sp)
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}

@Composable
private fun ProtectionField(value: String, onValue: (String) -> Unit, label: String) {
    OutlinedTextField(value, onValue, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
}

@Composable
private fun ProtectionStatus(label: String, passes: Boolean) {
    Text("$label: ${if (passes) "VERIFICACIÓN PRELIMINAR OK" else "REVISAR"}",
        color = if (passes) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
}

private fun protectionFmt(value: Double) = String.format(Locale.US, "%.2f", value)
