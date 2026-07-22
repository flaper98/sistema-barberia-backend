package com.barberia.modules.loyalty.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgregarSelloRequest {

    private Integer ventaId;

    @Size(max = 255)
    private String motivo;

    // Cuantas "ganancias" otorga este sello (ej. una venta con 2 servicios
    // que suman sello son 2, no 1) -- se multiplica por sellosPorGanancia
    // de la config. Null/no mandado = 1 (el uso manual desde Fidelizacion,
    // boton "Agregar sello", no manda este campo).
    @Min(1)
    private Integer cantidad;
}
