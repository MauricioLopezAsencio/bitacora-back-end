-- ============================================================
-- V9 – Tabla de correos para notificaciones
-- Gestiona los destinatarios de correos de recordatorios y bitácora
-- ============================================================

CREATE TABLE IF NOT EXISTS ct_correo_notificacion (
    id                      BIGSERIAL     NOT NULL,
    ds_nombre               VARCHAR(255)  NOT NULL,
    ds_correo               VARCHAR(255)  NOT NULL,
    bo_activo               BOOLEAN       NOT NULL DEFAULT true,
    bo_recordatorios        BOOLEAN       NOT NULL DEFAULT true,
    bo_bitacora             BOOLEAN       NOT NULL DEFAULT true,
    ds_creado_por           VARCHAR(255)  NULL,
    ds_actualizado_por      VARCHAR(255)  NULL,
    fc_creacion             TIMESTAMP     NULL,
    fc_ultima_actualizacion TIMESTAMP     NULL,
    CONSTRAINT PK_ct_correo_notificacion             PRIMARY KEY (id),
    CONSTRAINT UQ_ct_correo_notificacion__ds_correo  UNIQUE (ds_correo)
);

-- Trigger INSERT: establece ambas fechas al momento de creación
CREATE OR REPLACE FUNCTION fn_tr_ct_correo_notificacion_insert()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fc_creacion             := NOW();
    NEW.fc_ultima_actualizacion := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger UPDATE: actualiza solo fc_ultima_actualizacion
CREATE OR REPLACE FUNCTION fn_tr_ct_correo_notificacion_update()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fc_ultima_actualizacion := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_ct_correo_notificacion_insert
    BEFORE INSERT ON ct_correo_notificacion
    FOR EACH ROW EXECUTE FUNCTION fn_tr_ct_correo_notificacion_insert();

CREATE TRIGGER tr_ct_correo_notificacion_update
    BEFORE UPDATE ON ct_correo_notificacion
    FOR EACH ROW EXECUTE FUNCTION fn_tr_ct_correo_notificacion_update();
