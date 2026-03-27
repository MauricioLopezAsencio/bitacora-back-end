-- ============================================================
-- V5 – Tabla de usuarios del sistema
-- Gestiona credenciales para autenticación JWT
-- ============================================================

CREATE TABLE IF NOT EXISTS ct_usuarios (
    id                      BIGSERIAL     NOT NULL,
    cv_usuario              VARCHAR(50)   NOT NULL,
    ds_password             VARCHAR(255)  NOT NULL,
    ds_nombre               VARCHAR(250)  NOT NULL,
    bo_activo               BOOLEAN       NOT NULL DEFAULT true,
    ds_creado_por           VARCHAR(255)  NULL,
    ds_actualizado_por      VARCHAR(255)  NULL,
    fc_creacion             TIMESTAMP     NULL,
    fc_ultima_actualizacion TIMESTAMP     NULL,
    CONSTRAINT PK_ct_usuarios           PRIMARY KEY (id),
    CONSTRAINT UQ_ct_usuarios__cv_usuario UNIQUE (cv_usuario)
);

-- Triggers de tracking de timestamps
CREATE OR REPLACE FUNCTION fn_tr_ct_usuarios_insert()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fc_creacion             := NOW();
    NEW.fc_ultima_actualizacion := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_tr_ct_usuarios_update()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fc_ultima_actualizacion := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_ct_usuarios_insert
    BEFORE INSERT ON ct_usuarios
    FOR EACH ROW EXECUTE FUNCTION fn_tr_ct_usuarios_insert();

CREATE TRIGGER tr_ct_usuarios_update
    BEFORE UPDATE ON ct_usuarios
    FOR EACH ROW EXECUTE FUNCTION fn_tr_ct_usuarios_update();
