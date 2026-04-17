# Reglas de Negocio — bt-backend

---

## 1. Autenticación con Bitácora / SCOCA

- La contraseña del usuario se envía a SCOCA convertida a **MD5**; nunca se envía en texto plano.
- El token obtenido tiene un TTL de **23 horas**. Se renueva automáticamente si SCOCA responde con `401`.
- El `idEmpleado` se extrae de la respuesta del login de SCOCA y se cachea junto con el token.
- Si SCOCA no devuelve token en la respuesta, se lanza un error en tiempo de ejecución.

---

## 2. Eventos de Calendario (Microsoft Graph)

- Solo se procesan eventos de tipo `occurrence` o `singleInstance`. Los eventos `seriesMaster` **se descartan**.
- Los días que coincidan con un **feriado mexicano** se descartan (ver sección 8).
- Todos los timestamps se convierten a **zona horaria America/Mexico_City** antes de procesarse.
- La modalidad del evento se determina por los asistentes:
  - Todos los asistentes tienen correo `@casystem.com.mx` → `"interna"`
  - Al menos un asistente externo → `"externa"`
- La paginación del Graph API (`@odata.nextLink`) se consume completa antes de retornar.

---

## 3. Clasificación de Actividades vs Sesiones

- Un evento del calendario se clasifica como **actividad** si su subject hace match con algún proyecto del empleado en SCOCA.
- Si no hace match, se clasifica como **sesión sin proyecto** (`idProyecto = "N/A"`).
- El match se realiza dividiendo el subject por `|`, `-`, `:` y espacios, y buscando las palabras resultantes (> 2 caracteres) en el fragmento de descripción del proyecto que aparece **después del primer guion**.

---

## 4. Reglas de visualización — Actividades con proyecto

- Se calculan las **franjas libres** del evento restando todos los registros existentes en SCOCA para ese día (de cualquier tipo).
- Si todas las franjas quedan cubiertas → la actividad **no se muestra**.
- Si hay franjas parcialmente libres → se muestran solo los tramos no cubiertos.

---

## 5. Reglas de visualización — Sesiones sin proyecto

- Una sesión **se oculta** únicamente si SCOCA tiene un registro con **exactamente** el mismo `horaInicio` y `horaFin`.
- Un bloque mayor en SCOCA (ej. `9:20–17:00`) **no oculta** una sesión menor contenida (ej. `12:00–12:30`).
- Si hay dos sesiones idénticas en el calendario y una ya está registrada en SCOCA con el mismo horario exacto, ambas se ocultan.
- Si ninguna está registrada, ambas se muestran.

---

## 6. Registro de actividades con anti-traslape

Al registrar una actividad en SCOCA, se verifica si existe algún registro que se traslape con el nuevo horario. Para cada traslape:

| Caso | Acción |
|------|--------|
| El registro existente **envuelve** al nuevo (empieza antes y termina después) | Se edita el existente al tramo posterior (`newEnd–rEnd`) y se inserta el tramo anterior (`rStart–newStart`) |
| El existente empieza **antes** y termina dentro del nuevo | Se recorta su `horaFin` a `newStart` |
| El existente empieza **dentro** y termina **después** | Se mueve su `horaInicio` a `newEnd` |
| El existente queda **completamente dentro** del nuevo | Sin cambio (queda cubierto) |

Finalmente se hace el POST del nuevo registro. Si el horario ya está completamente cubierto, se retorna `422 HORARIO_CUBIERTO`.

---

## 7. Identificadores de sesión en SCOCA

Los siguientes IDs son constantes fijas que deben coincidir con el catálogo de SCOCA. Si el catálogo cambia, deben actualizarse en `ActividadService`:

| Constante | Valor | Significado |
|-----------|-------|-------------|
| `ID_SESION_INTERNA` | `1` | Sesión con asistentes internos |
| `ID_SESION_EXTERNA` | `2` | Sesión con asistentes externos |
| `ID_TIPO_ACTIVIDAD` | `3` | Tipo de actividad por defecto |

---

## 8. Feriados mexicanos

Los feriados están codificados en `FeriadosMexicoService` con formato `MM-dd`. Aplican para cualquier año.

| Fecha | Motivo |
|-------|--------|
| 01-01 | Año Nuevo |
| 02-02 | Constitución (recorrido) |
| 03-16 | Benito Juárez (recorrido) |
| 04-02 | Semana Santa (empresa) |
| 04-03 | Semana Santa (empresa) |
| 05-01 | Día del Trabajo |
| 09-16 | Independencia |
| 11-16 | Revolución (recorrido) |
| 12-25 | Navidad |

> Las fechas de Semana Santa son movibles y deben actualizarse **manualmente cada año**.

---

## 9. Préstamo de herramientas

- No se puede prestar una herramienta si `cantidadDisponible <= 0`.
- Al registrar un préstamo se **decrementa** `cantidadDisponible` de la herramienta.
- Al devolver una herramienta se **incrementa** `cantidadDisponible`.
- El estatus `false` = en préstamo / `true` = devuelta.
- **No se puede desactivar** una asignación (pasar de `true` a `false`). Si el empleado vuelve a tomar la herramienta, se debe crear una nueva asignación.
- Una asignación ya devuelta (`estatus = true`) no puede marcarse como devuelta de nuevo.
- El turno se normaliza: cualquier valor distinto de `VESPERTINO` o `NOCTURNO` se trata como `MATUTINO`.

---

## 10. Recordatorios de fin de turno

- Al registrar un préstamo se envía un **correo inmediato** de confirmación a todos los destinatarios con `boBitacora = true`.
- Se programa un **segundo correo** N minutos antes del fin del turno (N configurable en `ct_parametro_sistema`, por defecto 30).
- El segundo correo solo se envía si la herramienta **no ha sido devuelta** al momento de dispararse.
- Al reiniciar el servidor, los recordatorios pendientes de préstamos activos se **re-agendan automáticamente** (`@PostConstruct`).

### Cálculo del fin de turno

Los horarios se leen desde `ct_turno_config`. Si no existe la configuración, se usan estos valores por defecto:

| Turno | Fin por defecto |
|-------|----------------|
| MATUTINO | 14:00 |
| VESPERTINO | 22:00 |
| NOCTURNO | 06:00 (día siguiente) |

- Para **NOCTURNO**: si la asignación ocurrió antes de las `horaFin`, el turno termina ese mismo día; si ocurrió después, termina al día siguiente.
- Si el fin calculado ya pasó, se usa `fcAsignacion + duracionTurnoHoras` como respaldo.

### Validación al configurar turnos

- Para `MATUTINO` y `VESPERTINO`: `horaFin > horaInicio` (no puede cruzar medianoche).
- Para `NOCTURNO`: `horaFin < horaInicio` (cruza medianoche, obligatorio).

---

## 11. Destinatarios de correo

Hay dos tipos de destinatarios en `ct_correo_notificacion`:

| Flag | Recibe |
|------|--------|
| `boRecordatorios = true` | Correo de recordatorio N minutos antes del fin de turno |
| `boBitacora = true` | Correo inmediato al registrar un préstamo |

- Un destinatario con `boActivo = false` **no recibe ningún correo**, independientemente de sus flags.
- No se pueden registrar dos destinatarios con el mismo correo electrónico.

---

## 12. Estadísticas mensuales

- Los **días hábiles** son lunes a viernes, excluyendo feriados mexicanos (sección 8).
- `horasEsperadas = diasHabiles × 8`
- `horasRegistradas` = suma de la duración de todos los registros SCOCA del mes (`horaFin - horaInicio`), en horas.
- `porcentaje = (horasRegistradas / horasEsperadas) × 100`, máximo `100%`.
- Un día se cuenta como "con registro" si SCOCA devuelve al menos un registro para ese día.

---

## 13. Empleados

- El nombre del empleado se almacena siempre en **mayúsculas**.
- No existe lógica de unicidad por nombre; la nómina es el identificador de negocio.
