# CASOS_FEATURES.md

Registro de tareas de implementación por caso de uso. Solo features; sin pruebas unitarias.

## Nomenclatura de tipos

| Letra | Tipo |
|-------|------|
| L | Gestión de datos (DDL, migrations, seed) |
| A | Endpoint REST (backend) |
| C | Integración externa (correo, API, FTP) |
| H | Proceso automatizado / scheduler |
| N | Seguridad / control de acceso |
| K | Interfaz Angular (pantalla, modal, componente) |

---

## CU-1 — Estadísticas mensuales de bitácora

### Frontend

| ID | Feature | Tipo | Complejidad | Estimado (min) |
|----|---------|------|-------------|----------------|
| CU1-K1 | K - Implementación de servicio Angular: método `obtenerEstadisticasMes()` que llama POST `/api/v1/estadisticas/mes` con username, password, mes y anio | K | Baja | 30 |
| CU1-K2 | K - Implementación de interfaz: selector de mes y año con controles de formulario reactivo y validación de campos requeridos | K | Baja | 40 |
| CU1-K3 | K - Implementación de interfaz: cards de resumen con diasHabiles, diasConRegistro, horasEsperadas y horasRegistradas del mes consultado | K | Media | 80 |
| CU1-K4 | K - Implementación de interfaz: indicador visual de porcentaje (barra de progreso o gráfica circular) que muestra horasRegistradas vs horasEsperadas | K | Media | 90 |
| CU1-K5 | K - Implementación de interfaz: estado de carga (spinner) mientras se consulta el mes y recarga automática tras cada registro individual o masivo | K | Baja | 40 |
| CU1-K6 | K - Implementación de interfaz: notificaciones toast de error cuando falla la consulta al servicio de estadísticas | K | Baja | 25 |

---
