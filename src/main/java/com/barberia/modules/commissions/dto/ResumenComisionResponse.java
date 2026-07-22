package com.barberia.modules.commissions.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

// Vista previa de lo que un barbero tiene acumulado y todavia NO esta en
// ningun corte -- lo que se generaria si se hiciera un corte ahora mismo
// para el rango de fechas pedido.
@Getter
@Builder
public class ResumenComisionResponse {
    private final Integer barberoId;
    private final String barberoNombre;
    private final LocalDate fechaDesde;
    private final LocalDate fechaHasta;
    private final BigDecimal baseServicios;
    private final BigDecimal baseProductos;
    private final BigDecimal comisionServicios;
    private final BigDecimal comisionProductos;
    private final BigDecimal propinasPendientes;
    private final BigDecimal descuentosPendientes;
    private final BigDecimal totalEstimado;
}
