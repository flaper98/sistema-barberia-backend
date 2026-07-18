-- Migración V5: seed de configuración de fidelización
-- Impacto: agrega datos, no toca estructura.
-- Motivo: FidelizacionConfig (id=1) y el catálogo de recompensas nunca se
-- sembraron -- el endpoint de configuración devolvía 404 y agregarSello()
-- no tenía de dónde leer sellosNecesarios/sellosPorGanancia.

INSERT INTO fidelizacion_config (id, sellos_necesarios, sellos_por_ganancia, descripcion_recompensa, activo, updated_at)
VALUES (1, 5, 1, 'Corte de cabello gratis', true, NOW())
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO recompensas (tipo, descripcion, sellos_requeridos, activo)
SELECT 'CORTE_GRATIS', 'Corte de cabello gratis', 5, true
WHERE NOT EXISTS (SELECT 1 FROM recompensas WHERE tipo = 'CORTE_GRATIS');
