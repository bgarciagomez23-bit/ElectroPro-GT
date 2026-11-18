# ElectroPro GT

Aplicación de apoyo para cálculos de ingeniería eléctrica, orientada a proyectos profesionales en Guatemala.

## Aplicación web de cálculos
La versión web inicial incluye:
- Cálculo de corriente, kW y kVA en sistemas monofásicos y trifásicos.
- Evaluación de ampacidad mediante factores de corrección ingresados por el usuario.
- Cálculo de caída de tensión con resistencia y reactancia del circuito.
- Evaluación preliminar de interruptores, ampacidad y capacidad interruptiva.

La interfaz está disponible como sitio estático y se puede instalar como acceso directo desde el navegador del teléfono.

## Publicación
El despliegue está automatizado con GitHub Actions. En GitHub, habilitar **Settings → Pages → Build and deployment → Source: GitHub Actions**. Tras completarse el workflow, la aplicación estará en:
https://bgarciagomez23-bit.github.io/ElectroPro-GT/

## Aplicación Android
El repositorio también conserva la base Android prevista con Kotlin y Jetpack Compose, MVVM, Room/SQLite y sincronización futura.

## Normativa y seguridad
NEC como referencia principal; IEEE, IEC, ANSI/NETA y normativa guatemalteca como referencias complementarias según corresponda. Los resultados de esta versión son cálculos de apoyo y no certifican cumplimiento normativo. Verificar edición adoptada, artículos y tablas aplicables, método de instalación, temperatura, agrupamiento, terminales, cortocircuito y datos reales del equipo antes de aprobar diseños.

No se incluye selección automática de calibre por tabla normativa ni dictamen de cumplimiento. La ampacidad base y los parámetros R/X deben ser ingresados y verificados por el profesional responsable.
