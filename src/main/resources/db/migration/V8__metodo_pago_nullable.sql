-- El barbero ahora genera la solicitud de venta sin elegir cliente ni
-- metodo de pago (eso lo asigna recepcion al aceptar y cobrar). cliente_id
-- ya era nullable; metodo_pago no lo era -- lo dejamos nullable tambien
-- para que una solicitud PENDIENTE pueda existir sin ninguno de los dos
-- todavia asignado.
ALTER TABLE ventas MODIFY metodo_pago ENUM('EFECTIVO','PLIN','TARJETA','TRANSFERENCIA','YAPE') NULL;
