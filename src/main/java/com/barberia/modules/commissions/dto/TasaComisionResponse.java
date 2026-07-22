package com.barberia.modules.commissions.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

// Representa un servicio, producto o paquete del catalogo con su % de
// comision actual, para la pantalla Comisiones -> Tasas de comision.
@Getter
@Builder
public class TasaComisionResponse {
    private final Integer id;
    private final String nombre;
    private final BigDecimal precio;
    private final BigDecimal porcentajeComision;
}
