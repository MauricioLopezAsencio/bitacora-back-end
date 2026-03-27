# ROUTES — VALIA Core

Inventario de rutas HTTP registradas en los controladores Spring MVC.
Las columnas **Vista / Acción** indican la plantilla Thymeleaf renderizada o el comportamiento de la ruta.

---

## Autenticación — `AuthenticationController`

| Método | Ruta | Vista / Acción                        |
|--------|------|---------------------------------------|
| GET    | `/`  | `templates/auth/login.html`           |

---

## Dashboard — `DashboardController`

| Método | Ruta         | Vista / Acción                         |
|--------|--------------|----------------------------------------|
| GET    | `/dashboard` | `templates/dashboard/index.html`       |

**Atributos que se inyectan en el modelo:**

| Atributo        | Tipo                  | Descripción                                                    |
|-----------------|-----------------------|----------------------------------------------------------------|
| `kpisCentrales` | `List<CentralKpiDto>` | KPIs por central filtrados por permisos del usuario en sesión  |
| `username`      | `String`              | Nombre completo del usuario autenticado                        |

---

## Usuarios — `UsersController`

| Método | Ruta                 | Vista / Acción                                          |
|--------|----------------------|---------------------------------------------------------|
| GET    | `/users`             | `templates/users/index.html` — listado completo        |
| GET    | `/users/new`         | `redirect:/users` *(pendiente de implementar)*          |
| GET    | `/users/{id}/edit`   | `redirect:/users` *(pendiente de implementar)*          |
| POST   | `/users/{id}/delete` | `redirect:/users` *(pendiente de implementar)*          |

**Atributos que se inyectan en el modelo (`GET /users`):**

| Atributo   | Tipo            | Descripción                          |
|------------|-----------------|--------------------------------------|
| `users`    | `List<UserDto>` | Lista de usuarios del sistema        |
| `username` | `String`        | Nombre del usuario autenticado       |

---

## Administración — `AdministracionController`

### Roles y permisos

| Método | Ruta                    | Vista / Acción                                          |
|--------|-------------------------|---------------------------------------------------------|
| GET    | `/roles`                | `templates/admin/roles.html` — listado de roles        |
| POST   | `/roles`                | Crea un nuevo rol → `redirect:/roles`                  |
| POST   | `/roles/{id}`           | Edita un rol existente → `redirect:/roles`             |
| POST   | `/roles/{id}/status`    | Cambia estatus del rol → `redirect:/roles`             |

### Configuración del sistema (CU-005)

| Método | Ruta                                | Vista / Acción                                              |
|--------|-------------------------------------|-------------------------------------------------------------|
| GET    | `/settings`                         | `templates/admin/settings.html` — tabs Seguridad/Alertas/Reportes |
| POST   | `/settings/seguridad`               | Guarda configuración de seguridad → `redirect:/settings?tab=seguridad` |
| POST   | `/settings/alertas/{id}/editar`     | Edita destinatarios de una alerta → `redirect:/settings?tab=alertas` |
| POST   | `/settings/alertas/{id}/estatus`    | Cambia estatus de una alerta → `redirect:/settings?tab=alertas` |
| POST   | `/settings/reportes/{id}/formato`   | Cambia formato de descarga de un reporte → `redirect:/settings?tab=reportes` |

**Parámetros de filtro (GET `/settings`):**

| Parámetro        | Descripción                                 |
|------------------|---------------------------------------------|
| `tab`            | Pestaña activa: `seguridad`, `alertas`, `reportes` |
| `centralId`      | Filtro por central (alertas y reportes)     |
| `tipoAlertaId`   | Filtro por tipo de alerta                   |
| `activo`         | Filtro por estatus de alerta (`true`/`false`) |
| `tipoReporteId`  | Filtro por tipo de reporte                  |

---

## Reportes *(pendientes de implementar)*

| navKey      | Ruta                 | Menú         |
|-------------|----------------------|--------------|
| `saltillo`  | `/reports/saltillo`  | Saltillo CSO |
| `lomas`     | `/reports/lomas`     | Lomas CLR    |
| `anahuac`   | `/reports/anahuac`   | Anáhuac CAC  |
| `valle`     | `/reports/valle`     | Valle CVH    |
| `eea`       | `/reports/eea`       | EEA          |

---

## Auditoría — `AuditoriaController`

| Método | Ruta     | Vista / Acción                              |
|--------|----------|---------------------------------------------|
| GET    | `/audit` | `templates/monitoring/audit.html` — listado de eventos con filtros y paginación |

**Parámetros de filtro (GET `/audit`):**

| Parámetro    | Descripción                                            |
|--------------|--------------------------------------------------------|
| `fechaDesde` | Fecha de inicio del rango (formato ISO `yyyy-MM-dd`)   |
| `fechaHasta` | Fecha de fin del rango (formato ISO `yyyy-MM-dd`)      |
| `usuario`    | Filtro por nombre de usuario exacto                    |
| `modulo`     | Filtro por módulo del sistema                          |
| `page`       | Número de página base 0 (por defecto: 0)               |
| `size`       | Registros por página (por defecto: 10)                 |

**Atributos que se inyectan en el modelo:**

| Atributo            | Tipo                  | Descripción                                           |
|---------------------|-----------------------|-------------------------------------------------------|
| `registros`         | `List<AuditoriaDto>`  | Registros de la página actual                         |
| `totalElements`     | `long`                | Total de registros filtrados                          |
| `totalPages`        | `int`                 | Total de páginas                                      |
| `currentPage`       | `int`                 | Página activa (base 0)                                |
| `pageSize`          | `int`                 | Tamaño de página                                      |
| `fromEl` / `toEl`   | `long`                | Rango visible (e.g. 1–10)                             |
| `startPage` / `endPage` | `int`             | Rango de botones de paginación                        |
| `usuarios`          | `List<String>`        | Usuarios con actividad (para selector de filtro)      |
| `filtroFechaDesde`  | `String`              | Valor actual del filtro de fecha inicio               |
| `filtroFechaHasta`  | `String`              | Valor actual del filtro de fecha fin                  |
| `filtroUsuario`     | `String`              | Valor actual del filtro de usuario                    |
| `filtroModulo`      | `String`              | Valor actual del filtro de módulo                     |

---

## Autorización de Correcciones — `AutorizacionController`

| Método | Ruta                               | Vista / Acción                                                        |
|--------|------------------------------------|-----------------------------------------------------------------------|
| GET    | `/authorization`                   | `templates/monitoring/authorization.html` — tabla de incidencias con filtros y paginación |
| POST   | `/authorization/{id}/aprobar`      | Aprueba la corrección → `redirect:/authorization` con `successMsg`    |
| POST   | `/authorization/{id}/rechazar`     | Rechaza la corrección → `redirect:/authorization` con `successMsg`    |

**Parámetros de filtro (GET `/authorization`):**

| Parámetro   | Descripción                                                      |
|-------------|------------------------------------------------------------------|
| `central`   | Filtro por nombre de central exacto                              |
| `categoria` | Filtro por categoría: `Reporte incompleto` / `Reporte no generado` |
| `revisor`   | Búsqueda parcial por nombre de revisor (case-insensitive)        |
| `page`      | Número de página base 0 (por defecto: 0)                         |
| `size`      | Registros por página (por defecto: 10)                           |

**Atributos que se inyectan en el modelo:**

| Atributo             | Tipo                        | Descripción                                             |
|----------------------|-----------------------------|---------------------------------------------------------|
| `incidencias`        | `List<AutorizacionListDto>` | Registros de la página actual                           |
| `totalElements`      | `long`                      | Total de incidencias filtradas                          |
| `totalPages`         | `int`                       | Total de páginas                                        |
| `currentPage`        | `int`                       | Página activa (base 0)                                  |
| `pageSize`           | `int`                       | Tamaño de página                                        |
| `fromEl` / `toEl`    | `long`                      | Rango visible (e.g. 1–10)                               |
| `startPage` / `endPage` | `int`                    | Rango de botones de paginación                          |
| `centrales`          | `List<String>`              | Centrales disponibles para el selector de filtro        |
| `filtroCentral`      | `String`                    | Valor actual del filtro de central                      |
| `filtroCategoria`    | `String`                    | Valor actual del filtro de categoría                    |
| `filtroRevisor`      | `String`                    | Valor actual del filtro de revisor                      |

---

## Monitoreo y alertas *(pendientes de implementar — MonitoreoController)*

| navKey      | Ruta         | Menú        |
|-------------|--------------|-------------|
| `incidents` | `/incidents` | Incidencias |
| `bitacora`  | `/bitacora`  | Bitácora    |

---

## Errores — Spring Boot `BasicErrorController`

| Método | Ruta    | Vista / Acción                              |
|--------|---------|---------------------------------------------|
| GET    | `/error` *(404)* | `templates/error/404.html`       |

> Activado con `spring.mvc.throw-exception-if-no-handler-found: true` y
> `spring.web.resources.add-mappings: false` en `application.yaml`.

---

## Resumen global

| Método | Total |
|--------|-------|
| GET    | 13    |
| POST   | 11    |
| PUT    | —     |
| DELETE | —     |

---

> **Nota:** Las rutas marcadas como *pendiente de implementar* están mapeadas y registradas
> en el controlador pero redirigen al listado hasta que se habilite la capa de persistencia (JPA).
