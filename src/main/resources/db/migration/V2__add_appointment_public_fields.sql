-- Migración V2: Campos para reserva pública de citas
-- Impacto: Agrega 2 columnas nullable/default a la tabla citas. Sin downtime si el ORM no regenera el esquema.

ALTER TABLE citas
    ADD COLUMN appointment_code   VARCHAR(20)  NULL    UNIQUE  COMMENT 'Código único de reserva pública (ej: CITA-000125)',
    ADD COLUMN appointment_source VARCHAR(30)  NOT NULL DEFAULT 'INTERNAL'
        COMMENT 'Origen de la cita: INTERNAL | PUBLIC_WEB | PHONE | WALK_IN';

-- Índice para búsqueda rápida por código
CREATE INDEX idx_citas_appointment_code ON citas (appointment_code);
