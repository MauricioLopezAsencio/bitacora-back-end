<p align="center">
  <img src="https://miro.medium.com/v2/resize:fit:1400/format:webp/1*BBQq8yCFxaqneypPPpx2Jw.png" width="160" alt="Spring Boot" />
</p>

<h1 align="center">Bitácora Backend</h1>

<p align="center">
  API REST que automatiza el registro de actividades en el sistema SCO<br/>
  conectando el calendario de Microsoft con la bitácora corporativa.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot_3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
  <img src="https://img.shields.io/badge/AWS_EC2-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white" />
</p>

---

## ¿Qué hace?

Este backend actúa como middleware entre tres sistemas:

```
Microsoft Graph API  ──┐
                       ├──▶  bt-backend  ──▶  SCO / Bitácora
Scoca (SCO API)      ──┘         │
                                 │
                          PostgreSQL (Supabase)
                       (herramientas y empleados)
```

1. **Lee el calendario de Microsoft** del empleado para el rango de fechas solicitado
2. **Cruza los eventos con los proyectos** asignados en SCO, emparejando por palabras clave del asunto
3. **Filtra sesiones ya registradas** en SCO para no mostrar duplicados
4. **Registra actividades** respetando los horarios existentes — si hay traslape, divide automáticamente el intervalo en franjas libres

---

## Endpoints principales

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/v1/actividades` | Obtiene eventos del calendario emparejados con proyectos SCO |
| `POST` | `/api/v1/bitacora/actividades` | Registra una actividad en SCO (anti-traslape automático) |
| `POST` | `/api/v1/bitacora/proyectos/byEmpleado` | Lista proyectos asignados al empleado |
| `POST` | `/api/v1/bitacora/tipoActividad` | Catálogo de tipos de actividad |
| `GET`  | `/api/v1/bitacora/actividades/{id}` | Actividades por tipo |
| `POST` | `/api/v1/auth/login` | Login JWT propio del sistema |

---

## Configuración

### Variables de entorno requeridas

```env
SUPABASE_PASSWORD=   # Password de la base de datos PostgreSQL
JWT_SECRET=          # Secret Base64 para firmar tokens JWT (mín. 256 bits)
```

### Levantar local

```bash
# Con perfil local (sin Flyway ni base de datos remota)
mvn spring-boot:run -Dspring.profiles.active=local

# Con Docker
docker build -t bt-backend .
docker run -p 3000:3000 \
  -e SUPABASE_PASSWORD=tu_password \
  -e JWT_SECRET=tu_secret \
  bt-backend
```

El servidor corre en el puerto **3000**.

---

## Despliegue

El proyecto incluye un pipeline de CI/CD con **GitHub Actions** que despliega automáticamente en AWS EC2 en cada push a `master`.

```
git push origin master  →  GitHub Actions  →  SSH EC2  →  Docker rebuild
```
