-- Cambio de modelo de pago: los barberos ya no cobran sueldo fijo, cobran
-- por comision -- un % que define el administrador PARA CADA servicio y
-- PARA CADA producto del catalogo (no una tasa unica por barbero: no
-- todos los servicios/productos pagan lo mismo -- ver V10), mas propinas
-- que se suman a lo que se les paga, menos descuentos por consumo
-- personal (ej. una gaseosa del stock).

-- Nuevo tipo de movimiento de stock para el consumo interno de un barbero
-- (se descuenta del inventario igual que una salida, pero ademas queda
-- ligado al barbero para restarselo de su comision -- ver descuentos_consumo).
ALTER TABLE movimientos_stock
    MODIFY tipo_movimiento ENUM('AJUSTE','ENTRADA','SALIDA','CONSUMO') NOT NULL;

-- Nueva categoria de Finanzas para cuando se marca un corte de comision
-- como pagado -- reemplaza el uso manual de SALARIO para pagos a barberos.
ALTER TABLE finanzas
    MODIFY categoria ENUM('ALQUILER','COMISION','MANTENIMIENTO','OTRO','SALARIO','SERVICIOS','SUMINISTROS','VENTA') NOT NULL;

-- Un "corte" es el resumen cerrado de un periodo de pago para un barbero:
-- comision de servicios + comision de productos + propinas - descuentos.
-- Mientras esta PENDIENTE se puede revisar; al marcarlo PAGADO queda fijo
-- (no se recalcula mas, aunque cambien las tasas de comision despues) y
-- genera un egreso en Finanzas.
CREATE TABLE cortes_comision (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    barbero_id INT NOT NULL,
    fecha_desde DATE NOT NULL,
    fecha_hasta DATE NOT NULL,
    base_servicios DECIMAL(12,2) NOT NULL DEFAULT 0,
    base_productos DECIMAL(12,2) NOT NULL DEFAULT 0,
    comision_servicios DECIMAL(12,2) NOT NULL DEFAULT 0,
    comision_productos DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_propinas DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_descuentos DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_a_pagar DECIMAL(12,2) NOT NULL DEFAULT 0,
    estado ENUM('PENDIENTE','PAGADO') NOT NULL DEFAULT 'PENDIENTE',
    fecha_pago DATETIME(6) NULL,
    notas VARCHAR(500) NULL,
    usuario_registro_id INT NULL,
    usuario_registro_nombre VARCHAR(200) NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_cortes_barbero FOREIGN KEY (barbero_id) REFERENCES barberos (id)
);
CREATE INDEX idx_cortes_barbero_estado ON cortes_comision (barbero_id, estado);

-- Propinas que el cliente le da directamente al barbero. Quedan sueltas
-- (corte_id NULL) hasta que un corte las agrupa -- asi no se cuentan dos
-- veces en el siguiente corte.
CREATE TABLE propinas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    barbero_id INT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    fecha DATE NOT NULL,
    notas VARCHAR(255) NULL,
    usuario_registro_id INT NULL,
    usuario_registro_nombre VARCHAR(200) NULL,
    corte_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_propinas_barbero FOREIGN KEY (barbero_id) REFERENCES barberos (id),
    CONSTRAINT fk_propinas_corte FOREIGN KEY (corte_id) REFERENCES cortes_comision (id)
);
CREATE INDEX idx_propinas_barbero_corte ON propinas (barbero_id, corte_id);

-- Descuentos por consumo personal (ej. una gaseosa) -- cada uno tambien
-- genera un movimiento de stock tipo CONSUMO (ver InventarioServiceImpl),
-- asi el inventario y la comision del barbero quedan sincronizados en un
-- solo paso.
CREATE TABLE descuentos_consumo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    barbero_id INT NOT NULL,
    producto_id INT NOT NULL,
    producto_nombre VARCHAR(150) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    monto_total DECIMAL(10,2) NOT NULL,
    fecha DATE NOT NULL,
    motivo VARCHAR(255) NULL,
    usuario_registro_id INT NULL,
    usuario_registro_nombre VARCHAR(200) NULL,
    corte_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_descuentos_barbero FOREIGN KEY (barbero_id) REFERENCES barberos (id),
    CONSTRAINT fk_descuentos_producto FOREIGN KEY (producto_id) REFERENCES productos (id),
    CONSTRAINT fk_descuentos_corte FOREIGN KEY (corte_id) REFERENCES cortes_comision (id)
);
CREATE INDEX idx_descuentos_barbero_corte ON descuentos_consumo (barbero_id, corte_id);

-- Nuevo modulo para la matriz de permisos configurable por rol (Usuarios ->
-- Roles y permisos). El seeder (RolPermisoSeeder) solo corre en el primer
-- arranque con la tabla vacia, asi que en una instalacion ya existente hay
-- que agregar el modulo nuevo a mano -- mismos defaults que usaria el
-- seeder: barbero ve pero no edita (solo lectura de su propia comision),
-- cajero/recepcion ven y editan (ellos registran propinas/descuentos y
-- generan/pagan cortes). ADMIN no necesita fila (bypassea la matriz).
ALTER TABLE rol_permisos
    MODIFY modulo ENUM('BARBEROS','CITAS','CLIENTES','COMISIONES','CONFIGURACION','DASHBOARD','FIDELIZACION','FINANZAS','INVENTARIO','PRODUCTOS','REPORTES','SERVICIOS','VENTAS') NOT NULL;

INSERT INTO rol_permisos (rol, modulo, puede_ver, puede_editar, puede_eliminar) VALUES
    ('BARBER', 'COMISIONES', 1, 0, 0),
    ('CASHIER', 'COMISIONES', 1, 1, 0),
    ('RECEPTION', 'COMISIONES', 1, 1, 0);
