# ElectroPro GT

Aplicación Android para ingeniería eléctrica, orientada a proyectos profesionales en Guatemala.

## Objetivo
Centralizar cálculos eléctricos, proyectos, inspecciones, pruebas, termografía, presupuestos y generación de informes técnicos.

## Arquitectura prevista
- Android: Kotlin y Jetpack Compose.
- Arquitectura: MVVM y Clean Architecture.
- Persistencia local: Room/SQLite.
- Backend y sincronización: Supabase (PostgreSQL, Auth y Storage).
- Integración continua: GitHub Actions.

## Normativa
NEC como referencia principal, complementada según el módulo y la jurisdicción aplicable por IEEE, IEC, ANSI/NETA y normativa guatemalteca vigente.

## Estado del proyecto
Repositorio base de ElectroPro GT. Los módulos de cálculo deberán implementarse y validarse individualmente antes de utilizarse en diseños o instalaciones reales. Cada referencia normativa debe indicar edición y sección aplicable.

## Seguridad
No incluir claves privadas ni claves service-role en la aplicación Android. Aplicar Row Level Security en Supabase y mantener revisión y aprobación profesional de los resultados técnicos.
