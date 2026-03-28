-- V8: Migra los turnos de 2 (DIA/NOCHE, 12 h) a 3 (MATUTINO/VESPERTINO/NOCTURNO, 8 h)

-- 1. Convertir valores existentes
UPDATE empleado_herramienta SET turno = 'MATUTINO' WHERE turno = 'DIA';
UPDATE empleado_herramienta SET turno = 'NOCTURNO'  WHERE turno = 'NOCHE';

-- 2. Actualizar el valor por defecto de la columna
ALTER TABLE empleado_herramienta ALTER COLUMN turno SET DEFAULT 'MATUTINO';

-- 3. Agregar restricción de dominio para los tres turnos válidos
ALTER TABLE empleado_herramienta
    ADD CONSTRAINT ck_empleado_herramienta__turno
        CHECK (turno IN ('MATUTINO', 'VESPERTINO', 'NOCTURNO'));
