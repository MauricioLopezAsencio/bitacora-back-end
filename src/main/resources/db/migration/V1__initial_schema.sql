-- ============================================================
-- V1 – Esquema inicial
-- Tablas: cat_empleados, cat_herramientas, empleado_herramienta
-- ============================================================

CREATE TABLE IF NOT EXISTS cat_empleados (
    id      BIGSERIAL    PRIMARY KEY,
    nombre  VARCHAR(250),
    nomina  BIGINT
);

CREATE TABLE IF NOT EXISTS cat_herramientas (
    id         BIGSERIAL    PRIMARY KEY,
    nombre     VARCHAR(250),
    categoria  VARCHAR(250),
    estatus    BOOLEAN
);

CREATE TABLE IF NOT EXISTS empleado_herramienta (
    id             BIGSERIAL PRIMARY KEY,
    fecha          DATE,
    estatus        BOOLEAN,
    empleado_id    BIGINT NOT NULL,
    herramienta_id BIGINT NOT NULL,
    CONSTRAINT fk_empleado_herramienta__cat_empleados
        FOREIGN KEY (empleado_id)    REFERENCES cat_empleados(id),
    CONSTRAINT fk_empleado_herramienta__cat_herramientas
        FOREIGN KEY (herramienta_id) REFERENCES cat_herramientas(id)
);
