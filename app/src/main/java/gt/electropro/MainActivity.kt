package gt.electropro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Accent = Color(0xFFFFB640)
private val Panel = Color(0xFF101C24)
private val AppBg = Color(0xFF080D12)
private val Muted = Color(0xFFA7B2BA)
private val Border = Color(0xFF263946)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ElectroProApp() }
    }
}

data class ModuleItem(val title: String, val subtitle: String, val symbol: String)

@Composable
fun ElectroProApp() {
    var selected by remember { mutableStateOf("Inicio") }
    val modules = listOf(
        ModuleItem("Potencia & corriente", "AC monofásica, trifásica y DC", "ϟ"),
        ModuleItem("Dimensionar conductor", "Ampacidad con correcciones", "∿"),
        ModuleItem("Caída de tensión", "Longitud, calibre y material", "⌁"),
        ModuleItem("Protección general", "Carga continua/no continua", "⬟"),
        ModuleItem("Canalización", "EMT, IMC, RMC y PVC", "◯"),
        ModuleItem("Ampacidad de conductor", "Capacidad según calibre y material", "▦")
    )
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Accent, background = AppBg, surface = Panel,
        onPrimary = Color.White, onBackground = Color.White, onSurface = Color.White
    )) {
        Scaffold(
            containerColor = AppBg,
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF10243A)) {
                    listOf("Inicio", "Ingeniería", "Proyectos", "Informes").forEach { item ->
                        NavigationBarItem(
                            selected = selected == item,
                            onClick = { selected = item },
                            icon = { Text(when(item) { "Inicio" -> "⌂"; "Ingeniería" -> "⚡"; "Proyectos" -> "▦"; else -> "▤" }) },
                            label = { Text(item) }
                        )
                    }
                }
            }
        ) { padding ->
            if (selected == "Cálculos eléctricos") {
                ElectricalCalculatorScreen(onBack = { selected = "Inicio" })
            } else if (selected == "Conductores") {
                VoltageDropScreen(onBack = { selected = "Inicio" })
            } else if (selected == "Ampacidad") {
                AmpacityScreen(onBack = { selected = "Inicio" })
            } else if (selected == "Protecciones") {
                ProtectionScreen(onBack = { selected = "Inicio" })
            } else if (selected == "Puesta a tierra") {
                GroundingScreen(onBack = { selected = "Inicio" })
            } else if (selected == "Transformadores") {
                TransformerScreen(onBack = { selected = "Inicio" })
            } else if (selected == "Subestaciones") {
                SubstationScreen(onBack = { selected = "Inicio" })
            } else if (selected == "Proyectos") {
                ProjectManagementScreen(onBack = { selected = "Inicio" })
            } else if (selected == "Inspecciones") {
                InspectionScreen(onBack = { selected = "Inicio" })
            } else if (selected == "Termografía") {
                ThermographyScreen(onBack = { selected = "Inicio" })
            } else if (selected == "Pruebas eléctricas") {
                ElectricalTestsScreen(onBack = { selected = "Inicio" })
            } else if (selected == "Informes") {
                ReportsScreen(onBack = { selected = "Inicio" })
            } else {
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(18.dp))
                Text("ELECTROPRO GT", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Ingeniería eléctrica", fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text("Herramientas técnicas para proyectos eléctricos", color = Color.LightGray, fontSize = 13.sp)
                Spacer(Modifier.height(20.dp))
                Spacer(Modifier.height(18.dp))\n                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 18.dp)
                ) {
                    items(modules) { module ->
                        Card(
                            onClick = { selected = module.title },
                            colors = CardDefaults.cardColors(containerColor = Panel),
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier.height(118.dp)
                        ) {
                            Column(Modifier.fillMaxSize().padding(13.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                Text(module.symbol, color = Accent, fontSize = 22.sp)
                                Column {
                                    Text(module.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(module.subtitle, color = Color.LightGray, fontSize = 10.sp, lineHeight = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}
