package com.barberia.modules.loyalty.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

// Representa un servicio, producto o paquete del catalogo con si suma o no
// sello de fidelizacion, para la pantalla Fidelizacion -> Que aplica sello.
@Getter
@Builder
public class ReglaFidelizacionResponse {
    private final Integer id;
    private final String nombre;
    private final BigDecimal precio;
    private final Boolean aplicaFidelizacion;
}
