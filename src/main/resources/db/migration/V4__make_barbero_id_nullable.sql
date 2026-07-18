-- Migración V4: barbero_id opcional en citas
-- Impacto: relaja una restriccion NOT NULL en la tabla citas. Sin downtime, no borra datos.
-- Motivo: las citas importadas desde el formulario público simple (barberia-arabes-web)
-- no incluyen selección de barbero -- quedan como PENDIENTE hasta que el staff asigne uno.

ALTER TABLE citas
    MODIFY COLUMN barbero_id INT NULL;
