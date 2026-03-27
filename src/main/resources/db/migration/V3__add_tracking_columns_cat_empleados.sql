-- ============================================================
-- V3 – Columnas de tracking y triggers para cat_empleados
-- Convencion: ds_creado_por, ds_actualizado_por, fc_creacion, fc_ultima_actualizacion
-- ============================================================

ALTER TABLE cat_empleados
    ADD COLUMN IF NOT EXISTS ds_creado_por           VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS ds_actualizado_por      VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS fc_creacion             TIMESTAMP(6) NULL,
    ADD COLUMN IF NOT EXISTS fc_ultima_actualizacion TIMESTAMP(6) NULL;

-- ── Trigger INSERT ────────────────────────────────────────────
CREATE OR REPLACE FUNCTION fn_tr_cat_empleados_insert()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.fc_creacion             := NOW();
    NEW.fc_ultima_actualizacion := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_cat_empleados_insert
    BEFORE INSERT ON cat_empleados
    FOR EACH ROW
EXECUTE FUNCTION fn_tr_cat_empleados_insert();

-- ── Trigger UPDATE ────────────────────────────────────────────
CREATE OR REPLACE FUNCTION fn_tr_cat_empleados_update()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.fc_ultima_actualizacion := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_cat_empleados_update
    BEFORE UPDATE ON cat_empleados
    FOR EACH ROW
EXECUTE FUNCTION fn_tr_cat_empleados_update();
