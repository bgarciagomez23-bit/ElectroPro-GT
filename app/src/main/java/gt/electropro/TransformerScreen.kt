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
import gt.electropro.domain.calculations.TransformerCalculator
import gt.electropro.domain.calculations.TransformerPhase
import java.util.Locale

@Composable
fun TransformerScreen(onBack: () -> Unit) {
    var power by remember { mutableStateOf("750") }
    var primaryVoltage by remember { mutableStateOf("13200") }
    var secondaryVoltage by remember { mutableStateOf("208") }
    var impedance by remember { mutableStateOf("5.75") }
    var load by remember { mutableStateOf("600") }
    var phase by remember { mutableStateOf(TransformerPhase.THREE_PHASE) }
    var error by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<gt.electropro.domain.calculations.TransformerCalculationResult?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("ELECTROPRO GT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Cálculo de transformadores", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Corrientes nominales, carga y estimación de falla en bornes secundarios.", fontSize = 12.sp)
        Text("Sistema", fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            RadioButton(selected = phase == TransformerPhase.THREE_PHASE, onClick = { phase = TransformerPhase.THREE_PHASE })
            Text("Trifásico")
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = phase == TransformerPhase.SINGLE_PHASE, onClick = { phase = TransformerPhase.SINGLE_PHASE })
            Text("Monofásico")
        }
        TransformerField(power, { power = it; error = null }, "Potencia nominal (kVA)")
        TransformerField(primaryVoltage, { primaryVoltage = it; error = null }, "Tensión primaria (V)")
        TransformerField(secondaryVoltage, { secondaryVoltage = it; error = null }, "Tensión secundaria (V)")
        TransformerField(impedance, { impedance = it; error = null }, "Impedancia de placa (%)")
        TransformerField(load, { load = it; error = null }, "Carga conectada (kVA)")
        Button(onClick = {
            try {
                result = TransformerCalculator.calculate(
                    power.toDouble(), primaryVoltage.toDouble(), secondaryVoltage.toDouble(),
                    impedance.toDouble(), load.toDouble(), phase
                )
                error = null
            } catch (e: Exception) {
                result = null
                error = e.message ?: "Verifica los datos ingresados."
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Calcular transformador") }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        result?.let {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("RESULTADOS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("Corriente nominal primaria: ${transformerFmt(it.primaryCurrentA)} A")
                    Text("Corriente nominal secundaria: ${transformerFmt(it.secondaryCurrentA)} A")
                    Text("Nivel de carga: ${transformerFmt(it.loadPercent)} %")
                    Text("Falla trifásica/monofásica estimada en bornes secundarios: ${transformerFmt(it.secondaryFaultCurrentKA)} kA")
                    Text("Impedancia utilizada: ${transformerFmt(it.transformerImpedancePercent)} %")
                }
            }
        }
        Text("ALCANCE: la corriente de falla es una estimación ideal limitada únicamente por la impedancia del transformador, suponiendo fuente primaria rígida y falla franca en bornes secundarios. No incorpora impedancia de red aguas arriba, cables, motores, contribuciones, X/R ni decremento DC. No sustituye un estudio de cortocircuito ni coordinación. La carga en kVA debe ser la demanda coincidente calculada, no necesariamente la suma de placas.", fontSize = 11.sp)
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}

@Composable
private fun TransformerField(value: String, onValue: (String) -> Unit, label: String) {
    OutlinedTextField(value, onValue, modifier = Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
}

private fun transformerFmt(value: Double) = String.format(Locale.US, "%.2f", value)
