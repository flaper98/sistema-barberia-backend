-- No toda venta la atiende un barbero -- a veces alguien compra o consume
-- algo (una gaseosa, un producto) sin que sea por un servicio de barberia,
-- y no corresponde forzarle un barbero ni un cliente registrado a esa
-- venta. El cliente ya era opcional (ver V1); ahora el barbero tambien.
ALTER TABLE ventas MODIFY barbero_id INT NULL;
ALTER TABLE ventas MODIFY barbero_nombre VARCHAR(200) NULL;
