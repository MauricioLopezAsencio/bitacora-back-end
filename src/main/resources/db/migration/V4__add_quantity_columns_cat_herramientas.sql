-- ============================================================
-- V4 – Contabilidad de unidades por herramienta
-- Agrega cantidad_total y cantidad_disponible a cat_herramientas
-- ============================================================

ALTER TABLE cat_herramientas
    ADD COLUMN IF NOT EXISTS cantidad_total      INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS cantidad_disponible INTEGER NOT NULL DEFAULT 1;

-- Recalcular disponibles según asignaciones activas (estatus = false = prestada)
UPDATE cat_herramientas h
SET cantidad_disponible = GREATEST(
    h.cantidad_total - (
        SELECT COUNT(*)
        FROM empleado_herramienta eh
        WHERE eh.herramienta_id = h.id
          AND eh.estatus = false
    ),
    0
);
