package com.barberia.modules.sales.dto;

import com.barberia.modules.sales.entity.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Edicion de los datos generales de una venta ya registrada (solo ADMIN).
 * No permite tocar los items -- para eso hay que anular y volver a registrar.
 */
@Getter
@Setter
public class VentaUpdateRequest {

    @NotNull(message = "El cliente es obligatorio")
    private Integer clienteId;

    @NotNull(message = "El ID del barbero es obligatorio")
    private Integer barberoId;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
    private BigDecimal descuento = BigDecimal.ZERO;

    private String notas;
}
