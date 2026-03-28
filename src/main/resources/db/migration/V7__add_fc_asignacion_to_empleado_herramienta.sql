-- ============================================================
-- V7 – Agrega columna fc_asignacion a empleado_herramienta
-- Guarda el timestamp exacto del momento de la asignación
-- para poder re-agendar correos de fin de turno tras reinicio.
-- ============================================================

ALTER TABLE empleado_herramienta
    ADD COLUMN IF NOT EXISTS fc_asignacion TIMESTAMP NULL;
