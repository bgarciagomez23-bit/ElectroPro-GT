package gt.electropro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gt.electropro.domain.calculations.ElectricalCalculator
import gt.electropro.domain.calculations.SystemPhase
import java.util.Locale

private val CalculatorAccent = Color(0xFF37A0D8)
private val CalculatorPanel = Color(0xFF1B344E)

@Composable
fun ElectricalCalculatorScreen(onBack: () -> Unit) {
    var phase by remember { mutableStateOf(SystemPhase.THREE_PHASE) }
    var powerKw by remember { mutableStateOf("50") }
    var voltage by remember { mutableStateOf("208") }
    var powerFactor by remember { mutableStateOf("0.90") }
    var error by remember { mutableStateOf<String?>(null) }
    var currentA by remember { mutableStateOf<Double?>(null) }
    var apparentKva by remember { mutableStateOf<Double?>(null) }
    var reactiveKvar by remember { mutableStateOf<Double?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("ELECTROPRO GT", color = CalculatorAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text("Calculadora de potencia AC", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Cálculo ideal para cargas monofásicas y trifásicas equilibradas. En trifásico, ingresa tensión línea-línea.",
            color = Color.LightGray, fontSize = 12.sp)

        Card(colors = CardDefaults.cardColors(containerColor = CalculatorPanel)) {
            Column(Modifier.padding(14.dp)) {
                Text("Tipo de sistema", fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = phase == SystemPhase.SINGLE_PHASE, onClick = { phase = SystemPhase.SINGLE_PHASE })
                    Text("Monofásico")
                    Spacer(Modifier.weight(1f))
                    RadioButton(selected = phase == SystemPhase.THREE_PHASE, onClick = { phase = SystemPhase.THREE_PHASE })
                    Text("Trifásico")
                }
                OutlinedTextField(
                    value = powerKw, onValueChange = { powerKw = it; error = null },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Potencia activa (kW)") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = voltage, onValueChange = { voltage = it; error = null },
                    modifier = Modifier.fillMaxWidth(), label = { Text(if (phase == SystemPhase.THREE_PHASE) "Tensión línea-línea (V)" else "Tensión (V)") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = powerFactor, onValueChange = { powerFactor = it; error = null },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Factor de potencia (0 a 1)") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        try {
                            val p = powerKw.toDouble() * 1000.0
                            val v = voltage.toDouble()
                            val fp = powerFactor.toDouble()
                            val i = ElectricalCalculator.currentFromActivePower(p, v, fp, phase)
                            val quantities = ElectricalCalculator.powerFromVoltageCurrent(v, i, fp, phase)
                            currentA = i
                            apparentKva = quantities.apparentPowerVA / 1000.0
                            reactiveKvar = quantities.reactivePowerVar / 1000.0
                            error = null
                        } catch (exception: Exception) {
                            currentA = null
                            apparentKva = null
                            reactiveKvar = null
                            error = exception.message ?: "Verifica los datos ingresados."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Calcular") }
            }
        }

        error?.let {
            Text("Dato inválido: $it", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        currentA?.let { current ->
            Card(colors = CardDefaults.cardColors(containerColor = CalculatorPanel)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("RESULTADOS", color = CalculatorAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    ResultLine("Corriente calculada", "${formatValue(current)} A")
                    ResultLine("Potencia aparente", "${formatValue(apparentKva ?: 0.0)} kVA")
                    ResultLine("Potencia reactiva", "${formatValue(reactiveKvar ?: 0.0)} kvar")
                    Text("Resultado teórico. No representa por sí solo un dimensionamiento NEC de conductores o protecciones.",
                        color = Color.LightGray, fontSize = 11.sp)
                }
            }
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver al inicio") }
    }
}

@Composable
private fun ResultLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.LightGray)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

private fun formatValue(value: Double): String = String.format(Locale.US, "%.2f", value)
