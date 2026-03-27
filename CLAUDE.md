# Convención Oficial de Nomenclatura – Modelado de Datos (SQL Server 2022)

## Contexto
Este archivo define las reglas obligatorias de nomenclatura para todo el modelado de datos del proyecto en SQL Server 2022. Claude debe aplicar estas reglas en cualquier generación de DDL, scripts de migración Flyway, entidades JPA, vistas o consultas SQL.

---

## 1. Prefijos de tablas (obligatorio)

| Prefijo | Tipo de tabla |
|---------|--------------|
| `ct_`   | Catálogos / maestros |
| `mv_`   | Movimientos / transacciones (cabecera) |
| `dt_`   | Detalle (líneas dependientes de cabecera) |
| `h_`    | Historia (versionado / vigencia) |
| `bt_`   | Bitácora (eventos, trazas, logs funcionales) |

**Formato:** `<prefijo>_<entidad>[_<subentidad>]`

**Reglas:**
- Todo en minúsculas
- snake_case
- Sin acentos, sin "ñ", sin caracteres especiales
- Sin abreviaciones no estándar

**Ejemplos válidos:**
```
ct_cliente, ct_estatus_usuario
mv_venta, mv_pago
dt_venta_partida, dt_pago_detalle
h_cliente, h_usuario_rol
bt_integracion_api, bt_error_servicio
```

---

## 2. Llave primaria (PK)

- Todas las tablas tienen una PK llamada exactamente **`id`** (sin sufijos, sin prefijos)
- Tipo recomendado: `BIGINT IDENTITY(1,1)` o `INT IDENTITY(1,1)`
- Constraint: `PK_<tabla>`

```sql
-- Correcto
id BIGINT IDENTITY(1,1) NOT NULL,
CONSTRAINT PK_ct_usuarios PRIMARY KEY (id)

-- Incorrecto
id_usuario, usuario_id, pk_id
```

---

## 3. Llaves foráneas (FK)

- Columna FK: `id_<entidad_referenciada>`
- Constraint: `FK_<tabla_hija>__<tabla_padre>` (doble guion bajo)

```sql
-- Columnas FK
id_cliente      -- referencia a ct_cliente
id_sucursal     -- referencia a ct_sucursal
id_venta        -- referencia a mv_venta

-- Constraints
CONSTRAINT FK_mv_venta__ct_cliente FOREIGN KEY (id_cliente) REFERENCES ct_cliente(id)
CONSTRAINT FK_dt_venta_partida__mv_venta FOREIGN KEY (id_venta) REFERENCES mv_venta(id)
```

---

## 4. Prefijos de columnas (obligatorio)

| Prefijo | Tipo funcional | Tipo SQL sugerido |
|---------|---------------|-------------------|
| `ds_`   | Descripciones, textos, nombres | `NVARCHAR` |
| `cv_`   | Claves de negocio, códigos | `NVARCHAR` corto |
| `fc_`   | Fechas y horas | `DATE` / `DATETIME2` |
| `bo_`   | Booleanos / banderas | `BIT` |
| `id_`   | Solo para FKs | `BIGINT` / `INT` |

**Ejemplos por prefijo:**
```sql
-- ds_ → descriptivos
ds_nombre, ds_descripcion, ds_observaciones, ds_correo

-- cv_ → claves de negocio (candidatos a UNIQUE)
cv_cliente, cv_rfc, cv_curp, cv_usuario

-- fc_ → fechas
fc_evento, fc_ultimo_login, fc_inicio, fc_fin

-- bo_ → booleanos (semántica afirmativa, sin doble negación)
bo_activo, bo_visible, bo_actual, bo_aprobado

-- INCORRECTO
bo_no_aprobado  -- doble negación, no permitido
tokenpass       -- sin prefijo, no permitido
```

---

## 5. Campos de vigencia / historia

Cuando aplique versionado o vigencia:

```sql
fc_inicio   DATETIME2 NOT NULL,          -- obligatorio
fc_fin      DATETIME2 NULL,              -- opcional (NULL = registro abierto)
bo_actual   BIT NOT NULL DEFAULT 1       -- opcional, marca registro vigente
```

Las tablas históricas deben usar prefijo `h_`.

---

## 6. Constraints e índices

### Unique Constraint
```
UQ_<tabla>__<columna>

-- Ejemplos
UQ_ct_usuarios__cv_usuario
UQ_ct_cliente__cv_rfc
```

### Índices
```
IX_<tabla>__<col1>[_<col2>...]

-- Ejemplos
IX_mv_venta__id_cliente_fc_evento
IX_ct_usuarios__id_estatus_usuario
```

---

## 7. Campos de tracking (obligatorio en TODAS las tablas)

Cada tabla debe incluir los siguientes campos al final de su definición:

```sql
ds_creado_por           NVARCHAR(255) NULL,
ds_actualizado_por      NVARCHAR(255) NULL,
fc_creacion             DATETIME2(7)  NULL,
fc_ultima_actualizacion DATETIME2(7)  NULL,
```

### Reglas de llenado
- `ds_creado_por` / `ds_actualizado_por`: los setea la **aplicación** (usuario autenticado)
- `fc_creacion` / `fc_ultima_actualizacion`: los setea un **trigger** automáticamente

### Triggers obligatorios por tabla

Cada tabla debe tener exactamente **dos triggers**:

**Trigger INSERT** — establece ambas fechas al momento de creación:
```sql
CREATE TRIGGER tr_{tabla}_insert
ON {tabla}
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @ahora DATETIME2(7) = SYSDATETIME();
UPDATE {tabla}
SET    fc_creacion             = @ahora,
    fc_ultima_actualizacion = @ahora
WHERE  id IN (SELECT id FROM inserted);
END;
GO
```

**Trigger UPDATE** — actualiza solo `fc_ultima_actualizacion`. Usa `TRIGGER_NESTLEVEL()` para evitar recursión:
```sql
CREATE TRIGGER tr_{tabla}_update
ON {tabla}
AFTER UPDATE
                            AS
BEGIN
    SET NOCOUNT ON;
    IF TRIGGER_NESTLEVEL() > 1 RETURN;
UPDATE {tabla}
SET    fc_ultima_actualizacion = SYSDATETIME()
WHERE  id IN (SELECT id FROM inserted);
END;
GO
```

> **Nota para tablas N:M** (sin PK `id`): el UPDATE en los triggers debe usar la clave compuesta en lugar de `id`. Ver ejemplo en sección 8.

### Nomenclatura de triggers
```
tr_{tabla}_insert
tr_{tabla}_update
```

---

## 8. Ejemplos de referencia rápida

### Catálogo
```sql
CREATE TABLE ct_estatus_usuario (
                                    id                      INT IDENTITY(1,1)  NOT NULL,
                                    cv_estatus_usuario      NVARCHAR(20)       NOT NULL,
                                    ds_estatus_usuario      NVARCHAR(100)      NOT NULL,
                                    bo_activo               BIT                NOT NULL DEFAULT 1,
                                    ds_creado_por           NVARCHAR(255)          NULL,
                                    ds_actualizado_por      NVARCHAR(255)          NULL,
                                    fc_creacion             DATETIME2(7)           NULL,
                                    fc_ultima_actualizacion DATETIME2(7)           NULL,
                                    CONSTRAINT PK_ct_estatus_usuario PRIMARY KEY (id),
                                    CONSTRAINT UQ_ct_estatus_usuario__cv_estatus_usuario UNIQUE (cv_estatus_usuario)
);
GO

CREATE TRIGGER tr_ct_estatus_usuario_insert
    ON ct_estatus_usuario AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @ahora DATETIME2(7) = SYSDATETIME();
UPDATE ct_estatus_usuario
SET    fc_creacion = @ahora, fc_ultima_actualizacion = @ahora
WHERE  id IN (SELECT id FROM inserted);
END;
GO

CREATE TRIGGER tr_ct_estatus_usuario_update
    ON ct_estatus_usuario AFTER UPDATE
                                    AS
BEGIN
    SET NOCOUNT ON;
    IF TRIGGER_NESTLEVEL() > 1 RETURN;
UPDATE ct_estatus_usuario
SET    fc_ultima_actualizacion = SYSDATETIME()
WHERE  id IN (SELECT id FROM inserted);
END;
GO
```

### Movimiento
```sql
CREATE TABLE mv_venta (
                          id                      BIGINT IDENTITY(1,1) NOT NULL,
                          id_cliente              BIGINT               NOT NULL,
                          id_sucursal             BIGINT               NOT NULL,
                          fc_evento               DATETIME2            NOT NULL,
                          ds_observaciones        NVARCHAR(500)            NULL,
                          ds_creado_por           NVARCHAR(255)            NULL,
                          ds_actualizado_por      NVARCHAR(255)            NULL,
                          fc_creacion             DATETIME2(7)             NULL,
                          fc_ultima_actualizacion DATETIME2(7)             NULL,
                          CONSTRAINT PK_mv_venta PRIMARY KEY (id),
                          CONSTRAINT FK_mv_venta__ct_cliente  FOREIGN KEY (id_cliente)  REFERENCES ct_cliente(id),
                          CONSTRAINT FK_mv_venta__ct_sucursal FOREIGN KEY (id_sucursal) REFERENCES ct_sucursal(id)
);
GO

CREATE TRIGGER tr_mv_venta_insert
    ON mv_venta AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @ahora DATETIME2(7) = SYSDATETIME();
UPDATE mv_venta
SET    fc_creacion = @ahora, fc_ultima_actualizacion = @ahora
WHERE  id IN (SELECT id FROM inserted);
END;
GO

CREATE TRIGGER tr_mv_venta_update
    ON mv_venta AFTER UPDATE
                          AS
BEGIN
    SET NOCOUNT ON;
    IF TRIGGER_NESTLEVEL() > 1 RETURN;
UPDATE mv_venta
SET    fc_ultima_actualizacion = SYSDATETIME()
WHERE  id IN (SELECT id FROM inserted);
END;
GO
```

### Tabla N:M (clave compuesta — trigger usa JOIN en lugar de id)
```sql
CREATE TABLE dt_venta_partida (
                                  id_venta                BIGINT        NOT NULL,
                                  id_producto             BIGINT        NOT NULL,
                                  ds_creado_por           NVARCHAR(255)     NULL,
                                  ds_actualizado_por      NVARCHAR(255)     NULL,
                                  fc_creacion             DATETIME2(7)      NULL,
                                  fc_ultima_actualizacion DATETIME2(7)      NULL,
                                  CONSTRAINT PK_dt_venta_partida PRIMARY KEY (id_venta, id_producto),
                                  CONSTRAINT FK_dt_venta_partida__mv_venta    FOREIGN KEY (id_venta)    REFERENCES mv_venta(id),
                                  CONSTRAINT FK_dt_venta_partida__ct_producto FOREIGN KEY (id_producto) REFERENCES ct_producto(id)
);
GO

CREATE TRIGGER tr_dt_venta_partida_insert
    ON dt_venta_partida AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @ahora DATETIME2(7) = SYSDATETIME();
UPDATE t
SET    t.fc_creacion = @ahora, t.fc_ultima_actualizacion = @ahora
    FROM   dt_venta_partida t
    INNER  JOIN inserted i ON t.id_venta = i.id_venta AND t.id_producto = i.id_producto;
END;
GO

CREATE TRIGGER tr_dt_venta_partida_update
    ON dt_venta_partida AFTER UPDATE
                                  AS
BEGIN
    SET NOCOUNT ON;
    IF TRIGGER_NESTLEVEL() > 1 RETURN;
UPDATE t
SET    t.fc_ultima_actualizacion = SYSDATETIME()
    FROM   dt_venta_partida t
    INNER  JOIN inserted i ON t.id_venta = i.id_venta AND t.id_producto = i.id_producto;
END;
GO
```

### Historia
```sql
CREATE TABLE h_usuario_rol (
                               id                      BIGINT IDENTITY(1,1) NOT NULL,
                               id_usuario              BIGINT               NOT NULL,
                               id_rol                  BIGINT               NOT NULL,
                               fc_inicio               DATETIME2            NOT NULL,
                               fc_fin                  DATETIME2                NULL,
                               bo_actual               BIT                  NOT NULL DEFAULT 1,
                               ds_creado_por           NVARCHAR(255)            NULL,
                               ds_actualizado_por      NVARCHAR(255)            NULL,
                               fc_creacion             DATETIME2(7)             NULL,
                               fc_ultima_actualizacion DATETIME2(7)             NULL,
                               CONSTRAINT PK_h_usuario_rol PRIMARY KEY (id),
                               CONSTRAINT FK_h_usuario_rol__ct_usuario FOREIGN KEY (id_usuario) REFERENCES ct_usuario(id),
                               CONSTRAINT FK_h_usuario_rol__ct_rol     FOREIGN KEY (id_rol)     REFERENCES ct_rol(id)
);
GO

CREATE TRIGGER tr_h_usuario_rol_insert
    ON h_usuario_rol AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @ahora DATETIME2(7) = SYSDATETIME();
UPDATE h_usuario_rol
SET    fc_creacion = @ahora, fc_ultima_actualizacion = @ahora
WHERE  id IN (SELECT id FROM inserted);
END;
GO

CREATE TRIGGER tr_h_usuario_rol_update
    ON h_usuario_rol AFTER UPDATE
                               AS
BEGIN
    SET NOCOUNT ON;
    IF TRIGGER_NESTLEVEL() > 1 RETURN;
UPDATE h_usuario_rol
SET    fc_ultima_actualizacion = SYSDATETIME()
WHERE  id IN (SELECT id FROM inserted);
END;
GO
```

---

## 9. Checklist antes de generar cualquier DDL

- [ ] Nombre de tabla lleva prefijo correcto (`ct_`, `mv_`, `dt_`, `h_`, `bt_`)
- [ ] PK se llama exactamente `id`
- [ ] Constraint PK sigue formato `PK_<tabla>`
- [ ] Columnas FK siguen formato `id_<entidad>`
- [ ] Constraints FK siguen formato `FK_<hija>__<padre>` (doble guion bajo)
- [ ] Todas las columnas llevan prefijo (`ds_`, `cv_`, `fc_`, `bo_`, `id_`)
- [ ] Campos de tracking presentes al final: `ds_creado_por`, `ds_actualizado_por`, `fc_creacion`, `fc_ultima_actualizacion`
- [ ] Triggers `_insert` y `_update` creados por tabla
- [ ] No hay doble negación en booleanos (`bo_no_*`)
- [ ] Unique constraints siguen formato `UQ_<tabla>__<columna>`
- [ ] Índices siguen formato `IX_<tabla>__<columnas>`

 