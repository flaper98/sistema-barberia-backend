-- Migración V6: índices de rendimiento
-- Impacto: solo agrega índices (no toca datos, no hay downtime).
-- Motivo: casi todas las tablas solo tenían índice en la clave primaria --
-- cualquier filtro por fecha/estado/cliente/barbero (dashboard, reportes,
-- historial de ventas, historial de citas) hacía table scan completo. Con
-- pocos registros no se nota, pero degrada a medida que crece la base.

-- clientes: busqueda por telefono/nombre (Clientes, selector de Venta Rapida)
CREATE INDEX idx_clientes_telefono ON clientes (telefono);
CREATE INDEX idx_clientes_nombre   ON clientes (nombre, apellido);

-- ventas: filtro por rango de fechas + estado es el mas usado (dashboard,
-- reportes, historial); cliente_id/barbero_id para "ventas por cliente/barbero"
CREATE INDEX idx_ventas_fecha_estado ON ventas (fecha, estado);
CREATE INDEX idx_ventas_cliente_id   ON ventas (cliente_id);
CREATE INDEX idx_ventas_barbero_id   ON ventas (barbero_id);

-- citas: "citas de hoy" del dashboard corre en cada carga; filtros por
-- cliente/barbero se usan en las agendas
CREATE INDEX idx_citas_fecha      ON citas (fecha);
CREATE INDEX idx_citas_cliente_id ON citas (cliente_id);
CREATE INDEX idx_citas_barbero_id ON citas (barbero_id);

-- finanzas: resumen de caja por rango de fechas, y lookup por venta_id
-- (usado al anular/editar una venta para mantener sincronizado el ingreso)
CREATE INDEX idx_finanzas_fecha    ON finanzas (fecha);
CREATE INDEX idx_finanzas_venta_id ON finanzas (venta_id);

-- fidelizacion_movimientos: historial de sellos por cliente
CREATE INDEX idx_fidelizacion_mov_cliente_id ON fidelizacion_movimientos (cliente_id);

-- movimientos_stock: historial de movimientos por producto (Inventario)
CREATE INDEX idx_movimientos_stock_producto_id ON movimientos_stock (producto_id);
