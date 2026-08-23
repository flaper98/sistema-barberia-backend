-- Productos de cortesia que se entregan gratis al vender un servicio (ej. un
-- corte premium incluye una cerveza o agua de cortesia). Configuracion del
-- administrador, no un registro transaccional -- por eso lleva FK real a
-- servicios/productos (a diferencia de venta_items, que es un snapshot
-- historico). Al completarse una venta con ese servicio, VentaServiceImpl
-- descuenta el stock automaticamente (ver TipoMovimiento.CORTESIA).
CREATE TABLE servicio_cortesias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    servicio_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_cortesia_servicio FOREIGN KEY (servicio_id) REFERENCES servicios (id),
    CONSTRAINT fk_cortesia_producto FOREIGN KEY (producto_id) REFERENCES productos (id),
    CONSTRAINT uq_cortesia_servicio_producto UNIQUE (servicio_id, producto_id)
);

ALTER TABLE movimientos_stock
    MODIFY tipo_movimiento ENUM('AJUSTE','ENTRADA','SALIDA','CONSUMO','CORTESIA') NOT NULL;
