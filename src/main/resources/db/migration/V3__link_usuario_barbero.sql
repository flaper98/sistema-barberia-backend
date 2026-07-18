-- Migración V3: Vínculo entre usuarios (login) y barberos (staff)
-- Impacto: Agrega 1 columna nullable/unique a la tabla usuarios. Sin downtime si el ORM no regenera el esquema.

ALTER TABLE usuarios
    ADD COLUMN barbero_id INT NULL UNIQUE
        COMMENT 'Vínculo lógico (sin FK) hacia barberos.id; solo aplica a cuentas con rol=BARBER';

-- Índice para checks de autorización (@PreAuthorize) rápidos
CREATE INDEX idx_usuarios_barbero_id ON usuarios (barbero_id);
