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

private val Accent = Color(0xFF37A0D8)
private val Panel = Color(0xFF1B344E)
private val AppBg = Color(0xFF0B1724)

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
        ModuleItem("Cálculos eléctricos", "Corriente, potencia y demanda", "⚡"),
        ModuleItem("Conductores", "Ampacidad y caída de tensión", "〰"),
        ModuleItem("Protecciones", "Interruptores y coordinación", "▣"),
        ModuleItem("Puesta a tierra", "Resistividad y mallas", "⌁"),
        ModuleItem("Transformadores", "Capacidad y cortocircuito", "◈"),
        ModuleItem("Subestaciones", "Equipos y alimentadores", "▤"),
        ModuleItem("Inspecciones", "Hallazgos y fotografías", "✓"),
        ModuleItem("Termografía", "Temperaturas y anomalías", "◉"),
        ModuleItem("Pruebas eléctricas", "Aislamiento y continuidad", "⌗"),
        ModuleItem("Presupuestos", "Materiales y cuantificación", "Ｑ"),
        ModuleItem("Informes", "Memorias PDF y Excel", "▧"),
        ModuleItem("Proyectos", "Clientes y documentación", "▦")
    )
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Accent, background = AppBg, surface = Color(0xFF10243A),
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
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(18.dp))
                Text("ELECTROPRO GT", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Ingeniería eléctrica", fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text("Herramientas técnicas para proyectos eléctricos", color = Color.LightGray, fontSize = 13.sp)
                Spacer(Modifier.height(20.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Centro de ingeniería", color = Color.LightGray, fontSize = 13.sp)
                        Text("Bienvenido a ElectroPro GT", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text("Trabaja localmente y sincroniza tus proyectos cuando tengas conexión.",
                            color = Color(0xFFCFD9E4), fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text("MÓDULOS", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                LazyVerticalGrid(
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
