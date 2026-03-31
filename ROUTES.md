# ROUTES — bt-backend

Inventario de rutas HTTP registradas en los controladores Spring MVC.

> **Autenticación:** Todas las rutas excepto `/api/v1/auth/**` requieren header `Authorization: Bearer <token>`.

---

## Autenticación — `AuthController` (`/api/v1/auth`) — PÚBLICA

| Método | Ruta                   | Descripción                          |
|--------|------------------------|--------------------------------------|
| POST   | `/api/v1/auth/login`   | Login con usuario y contraseña → JWT |

**Body:**
```json
{ "user": "admin", "password": "admin123" }
```
**Response 200:**
```json
{
  "status": 200,
  "data": { "token": "eyJ...", "username": "admin" },
  "message": "Login exitoso"
}
```
**Response 401:**
```json
{ "status": 401, "message": "Credenciales incorrectas", "errorCode": "INVALID_CREDENTIALS" }
```

---

## Empleados — `EmpleadoController` (`/api/v1/empleados`)

| Método | Ruta                     | Descripción                                        |
|--------|--------------------------|----------------------------------------------------|
| GET    | `/api/v1/empleados`      | Lista todos los empleados ordenados por nombre     |
| GET    | `/api/v1/empleados/{id}` | Obtiene un empleado por su ID                      |
| POST   | `/api/v1/empleados`      | Crea un nuevo empleado                             |
| PUT    | `/api/v1/empleados/{id}` | Actualiza nombre y nómina de un empleado existente |
| DELETE | `/api/v1/empleados/{id}` | Elimina un empleado por su ID                      |

**Body POST/PUT:**
```json
{ "nombre": "JUAN PEREZ", "nomina": 100001 }
```

---

## Herramientas y Bitácora — `ProductController` (`api/v1/`)

| Método | Ruta                                | Descripción                                                                   |
|--------|-------------------------------------|-------------------------------------------------------------------------------|
| GET    | `/api/v1/dashboard`                 | Estadísticas de herramientas: total, prestadas, disponibles por categoría     |
| GET    | `/api/v1/products`                  | Lista todas las herramientas (incluye cantidad_total y cantidad_disponible)   |
| GET    | `/api/v1/productsActivos`           | Lista herramientas con estatus activo                                         |
| GET    | `/api/v1/bitacora`                  | Lista el histórico de asignaciones (bitácora)                                 |
| POST   | `/api/v1/saveHerramienta`           | Crea una nueva herramienta (campo `cantidadTotal` obligatorio)                |
| POST   | `/api/v1/asignar`                   | Asigna una herramienta a un empleado (valida disponibilidad)                  |
| PUT    | `/api/v1/actualizar`                | Marca asignación como devuelta e incrementa cantidad disponible               |
| PUT    | `/api/v1/inactivarHerramienta/{id}` | Activa o desactiva una herramienta (toggle)                                   |

**Body POST /saveHerramienta:**
```json
{ "nombre": "Taladro", "categoria": "Eléctrico", "estatus": true, "cantidadTotal": 2 }
```

**Response GET /dashboard:**
```json
{
  "status": 200,
  "data": {
    "totalTipos": 5,
    "totalUnidades": 12,
    "totalPrestadas": 3,
    "totalDisponibles": 9,
    "porCategoria": [
      { "categoria": "Eléctrico", "totalUnidades": 5, "prestadas": 2, "disponibles": 3 }
    ],
    "prestamosActivos": [
      { "id": 1, "nombreEmpleado": "Juan", "nombreHerramienta": "Multímetro", "fecha": "2026-03-22", "diasPrestado": 5, "alerta": "ROJO" },
      { "id": 3, "nombreEmpleado": "Pedro", "nombreHerramienta": "Pinzas",    "fecha": "2026-03-24", "diasPrestado": 3, "alerta": "AMARILLO" },
      { "id": 5, "nombreEmpleado": "Luis",  "nombreHerramienta": "Taladro",   "fecha": "2026-03-26", "diasPrestado": 1, "alerta": "VERDE" }
    ]
  }
}
```
Semáforo `alerta`: `VERDE` 0–2 días · `AMARILLO` 3–4 días · `ROJO` 5+ días

---

## Correos de Notificación — `CorreoNotificacionController` (`/api/v1/correos`)

| Método | Ruta                              | Descripción                                               |
|--------|-----------------------------------|-----------------------------------------------------------|
| GET    | `/api/v1/correos`                 | Lista todos los destinatarios de correo                   |
| GET    | `/api/v1/correos/{id}`            | Obtiene un destinatario por su ID                         |
| POST   | `/api/v1/correos`                 | Crea un nuevo destinatario                                |
| PUT    | `/api/v1/correos/{id}`            | Actualiza nombre, correo y preferencias de notificación   |
| DELETE | `/api/v1/correos/{id}`            | Elimina un destinatario por su ID                         |
| PUT    | `/api/v1/correos/{id}/toggle-activo` | Activa o desactiva un destinatario (toggle)            |

**Body POST/PUT:**
```json
{
  "dsNombre": "ING. Felix Hernandez",
  "dsCorreo": "felix@empresa.com",
  "boRecordatorios": true,
  "boBitacora": true
}
```

**Response GET /correos:**
```json
{
  "status": 200,
  "data": [
    {
      "id": 1,
      "dsNombre": "ING. Felix Hernandez",
      "dsCorreo": "felix@empresa.com",
      "boActivo": true,
      "boRecordatorios": true,
      "boBitacora": true
    }
  ]
}
```

> **Tipos de notificación:**
> - `boRecordatorios`: recibe correos 30 min antes de que termine el turno si la herramienta no fue devuelta
> - `boBitacora`: recibe correo de confirmación al momento de registrar un préstamo

---

## Configuración — `ConfiguracionController`

| Método | Ruta                                        | Alias aceptado                          | Descripción                                       |
|--------|---------------------------------------------|-----------------------------------------|---------------------------------------------------|
| GET    | `/api/v1/configuracion/turnos`              | `/api/v1/turnos`                        | Lista los 3 turnos con sus horarios configurados  |
| PUT    | `/api/v1/configuracion/turnos/{id}`         | `/api/v1/turnos/{id}`                   | Actualiza hora de inicio y fin de un turno        |
| GET    | `/api/v1/configuracion/parametros`          | `/api/v1/configuracion/recordatorio`    | Obtiene los parámetros del sistema                |
| PUT    | `/api/v1/configuracion/parametros`          | `/api/v1/configuracion/recordatorio`    | Actualiza el tiempo de anticipación del recordatorio |

**Body PUT /configuracion/turnos/{id}:**
```json
{ "horaInicio": 6, "horaFin": 14 }
```

**Body PUT /configuracion/parametros:**
```json
{ "minutosRecordatorio": 45 }
```

**Response GET /configuracion/turnos:**
```json
{
  "status": 200,
  "data": [
    { "id": 1, "cvTurno": "MATUTINO",   "horaInicio": 6,  "horaFin": 14, "dsHorario": "06:00 → 14:00" },
    { "id": 2, "cvTurno": "VESPERTINO", "horaInicio": 14, "horaFin": 22, "dsHorario": "14:00 → 22:00" },
    { "id": 3, "cvTurno": "NOCTURNO",   "horaInicio": 22, "horaFin": 6,  "dsHorario": "22:00 → 06:00" }
  ]
}
```

> **Regla de negocio:** NOCTURNO requiere `horaFin < horaInicio` (cruza medianoche).
> MATUTINO y VESPERTINO requieren `horaFin > horaInicio`.

---

## Resumen global

| Método | Total |
|--------|-------|
| GET    | 12    |
| POST   | 4     |
| PUT    | 7     |
| DELETE | 2     |
