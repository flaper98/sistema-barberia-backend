-- Migración V17: catálogo de premios administrable
-- Impacto: cambia el default de sellos necesarios de 5 a 6 y agrega un
-- premio inicial genérico al catálogo.
-- Motivo: el admin ahora puede crear/editar varios premios y elegir cuál
-- entregarle a cada cliente al momento de canjear, en vez de un único
-- premio fijo ("Corte de cabello gratis") -- ver FidelizacionServiceImpl
-- crearRecompensa/actualizarRecompensa/canjearRecompensa.

UPDATE fidelizacion_config SET sellos_necesarios = 6 WHERE id = 1;

INSERT INTO recompensas (tipo, descripcion, sellos_requeridos, activo)
SELECT 'PRODUCTO_GRATIS', 'Premio a elegir', 6, true
WHERE NOT EXISTS (SELECT 1 FROM recompensas);
