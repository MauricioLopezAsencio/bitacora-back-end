-- ============================================================
-- V10 – Configuración de turnos y parámetros del sistema
-- Permite ajustar horarios de turno y tiempo de recordatorio
-- ============================================================

-- ── Tabla: configuración de horarios por turno ──────────────
CREATE TABLE IF NOT EXISTS ct_turno_config (
    id                      BIGSERIAL     NOT NULL,
    cv_turno                VARCHAR(20)   NOT NULL,
    hora_inicio             INT           NOT NULL,
    hora_fin                INT           NOT NULL,
    ds_creado_por           VARCHAR(255)  NULL,
    ds_actualizado_por      VARCHAR(255)  NULL,
    fc_creacion             TIMESTAMP     NULL,
    fc_ultima_actualizacion TIMESTAMP     NULL,
    CONSTRAINT PK_ct_turno_config            PRIMARY KEY (id),
    CONSTRAINT UQ_ct_turno_config__cv_turno  UNIQUE (cv_turno),
    CONSTRAINT CK_ct_turno_config__cv_turno  CHECK  (cv_turno IN ('MATUTINO', 'VESPERTINO', 'NOCTURNO')),
    CONSTRAINT CK_ct_turno_config__hora_ini  CHECK  (hora_inicio BETWEEN 0 AND 23),
    CONSTRAINT CK_ct_turno_config__hora_fin  CHECK  (hora_fin    BETWEEN 0 AND 23)
);

CREATE OR REPLACE FUNCTION fn_tr_ct_turno_config_insert()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fc_creacion             := NOW();
    NEW.fc_ultima_actualizacion := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_tr_ct_turno_config_update()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fc_ultima_actualizacion := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_ct_turno_config_insert
    BEFORE INSERT ON ct_turno_config
    FOR EACH ROW EXECUTE FUNCTION fn_tr_ct_turno_config_insert();

CREATE TRIGGER tr_ct_turno_config_update
    BEFORE UPDATE ON ct_turno_config
    FOR EACH ROW EXECUTE FUNCTION fn_tr_ct_turno_config_update();

-- Seed: horarios iniciales (MATUTINO 06-14 / VESPERTINO 14-22 / NOCTURNO 22-06)
INSERT INTO ct_turno_config (cv_turno, hora_inicio, hora_fin, ds_creado_por)
VALUES
    ('MATUTINO',   6, 14, 'system'),
    ('VESPERTINO', 14, 22, 'system'),
    ('NOCTURNO',   22,  6, 'system');

-- ── Tabla: parámetros globales del sistema ──────────────────
CREATE TABLE IF NOT EXISTS ct_parametro_sistema (
    id                      BIGSERIAL     NOT NULL,
    minutos_recordatorio    INT           NOT NULL DEFAULT 30,
    ds_creado_por           VARCHAR(255)  NULL,
    ds_actualizado_por      VARCHAR(255)  NULL,
    fc_creacion             TIMESTAMP     NULL,
    fc_ultima_actualizacion TIMESTAMP     NULL,
    CONSTRAINT PK_ct_parametro_sistema              PRIMARY KEY (id),
    CONSTRAINT CK_ct_parametro_sistema__minutos     CHECK (minutos_recordatorio BETWEEN 1 AND 120)
);

CREATE OR REPLACE FUNCTION fn_tr_ct_parametro_sistema_insert()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fc_creacion             := NOW();
    NEW.fc_ultima_actualizacion := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_tr_ct_parametro_sistema_update()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fc_ultima_actualizacion := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_ct_parametro_sistema_insert
    BEFORE INSERT ON ct_parametro_sistema
    FOR EACH ROW EXECUTE FUNCTION fn_tr_ct_parametro_sistema_insert();

CREATE TRIGGER tr_ct_parametro_sistema_update
    BEFORE UPDATE ON ct_parametro_sistema
    FOR EACH ROW EXECUTE FUNCTION fn_tr_ct_parametro_sistema_update();

-- Seed: 30 minutos de recordatorio por defecto (id=1, fijo)
INSERT INTO ct_parametro_sistema (minutos_recordatorio, ds_creado_por)
VALUES (30, 'system');
