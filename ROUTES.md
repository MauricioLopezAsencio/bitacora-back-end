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

## Resumen global

| Método | Total |
|--------|-------|
| GET    | 8     |
| POST   | 3     |
| PUT    | 3     |
| DELETE | 1     |
