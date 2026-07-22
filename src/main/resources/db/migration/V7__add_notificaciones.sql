-- Notificaciones internas: al barbero cuando se le asigna una cita, y a
-- ADMIN/CASHIER/RECEPTION cuando un barbero genera una solicitud de venta
-- pendiente de cobro. Una fila por destinatario (no por rol), asi "leida"
-- es siempre por-usuario y nunca se pisa entre distintos usuarios del mismo
-- rol.
CREATE TABLE notificaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    mensaje VARCHAR(255) NOT NULL,
    entidad_tipo VARCHAR(30) NULL,
    entidad_id INT NULL,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    fecha DATETIME(6) NOT NULL,
    CONSTRAINT fk_notificaciones_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE INDEX idx_notificaciones_usuario_leida ON notificaciones (usuario_id, leida, fecha);
