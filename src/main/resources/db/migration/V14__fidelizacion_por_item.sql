-- Igual que con comisiones (V10), no todos los servicios/productos/paquetes
-- tienen por que sumar sello de fidelizacion -- el administrador elige
-- cuales desde Fidelizacion -> Que aplica sello.
-- El default preserva el comportamiento de siempre (antes de esta
-- migracion, CUALQUIER venta con al menos un servicio o paquete sumaba
-- sello, y un producto solo nunca sumaba): servicios y paquetes arrancan
-- en 1 (activo), productos en 0 -- nadie deja de ganar sellos que ya
-- venia ganando el dia que se aplique esta migracion.
ALTER TABLE servicios ADD COLUMN aplica_fidelizacion TINYINT(1) NOT NULL DEFAULT 1;
ALTER TABLE paquetes  ADD COLUMN aplica_fidelizacion TINYINT(1) NOT NULL DEFAULT 1;
ALTER TABLE productos ADD COLUMN aplica_fidelizacion TINYINT(1) NOT NULL DEFAULT 0;
