-- A veces cobran una venta con mas de un metodo a la vez (ej. una parte en
-- efectivo y otra por Yape) -- el desglose real vive en venta_pagos;
-- ventas.metodo_pago pasa a ser un resumen: el unico metodo si hay uno solo,
-- o MIXTO si hay dos o mas (sigue NULL para una solicitud PENDIENTE sin cobrar).
ALTER TABLE ventas
    MODIFY metodo_pago ENUM('EFECTIVO','PLIN','TARJETA','TRANSFERENCIA','YAPE','MIXTO') NULL;

CREATE TABLE venta_pagos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    venta_id INT NOT NULL,
    metodo_pago ENUM('EFECTIVO','PLIN','TARJETA','TRANSFERENCIA','YAPE') NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_venta_pagos_venta FOREIGN KEY (venta_id) REFERENCES ventas (id)
);
CREATE INDEX idx_venta_pagos_venta ON venta_pagos (venta_id);
