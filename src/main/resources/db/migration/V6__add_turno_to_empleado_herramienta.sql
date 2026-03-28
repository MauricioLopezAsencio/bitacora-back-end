-- ============================================================
-- V6 – Agrega columna turno a empleado_herramienta
-- Valores permitidos: 'DIA', 'NOCHE'
-- ============================================================

ALTER TABLE empleado_herramienta
    ADD COLUMN IF NOT EXISTS turno VARCHAR(10) NOT NULL DEFAULT 'DIA';
