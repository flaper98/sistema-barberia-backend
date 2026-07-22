-- No todos los servicios ni todos los productos pagan la misma comision
-- (ej. un corte simple puede pagar distinto que un alisado; una gaseosa
-- distinto que una crema premium) -- el administrador define el % de
-- cada uno individualmente desde Comisiones -> Tasas de comision.
-- Default 0: hasta que el admin configure una tasa real, ese item no
-- genera comision (mejor que asumirle un numero inventado).

ALTER TABLE servicios ADD COLUMN porcentaje_comision DECIMAL(5,2) NOT NULL DEFAULT 0.00;
ALTER TABLE productos ADD COLUMN porcentaje_comision DECIMAL(5,2) NOT NULL DEFAULT 0.00;
ALTER TABLE paquetes  ADD COLUMN porcentaje_comision DECIMAL(5,2) NOT NULL DEFAULT 0.00;
