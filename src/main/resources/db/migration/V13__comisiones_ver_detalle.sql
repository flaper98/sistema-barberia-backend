-- Ver el detalle por item de comisiones (ventas de un barbero por cada
-- servicio/producto/paquete, con fecha y comision) es sensible: por defecto
-- solo lo puede ver ADMIN (bypass ya existente, no se guarda en esta
-- tabla). Cajero/recepcion arrancan SIN este permiso -- el administrador
-- se lo puede otorgar despues desde Usuarios -> Roles y permisos si
-- quiere. Solo tiene sentido para el modulo COMISIONES; en el resto queda
-- en 0 sin usarse, igual que ya pasa con "puede_eliminar" para ese mismo
-- modulo (nunca existio un "eliminar corte").
ALTER TABLE rol_permisos
    ADD COLUMN puede_ver_detalle TINYINT(1) NOT NULL DEFAULT 0;
