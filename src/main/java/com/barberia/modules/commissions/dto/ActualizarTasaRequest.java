package com.barberia.modules.commissions.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ActualizarTasaRequest {

    @NotNull(message = "El porcentaje de comisión es obligatorio")
    @DecimalMin(value = "0.00", message = "El porcentaje de comisión no puede ser negativo")
    @DecimalMax(value = "100.00", message = "El porcentaje de comisión no puede superar 100")
    private BigDecimal porcentajeComision;
}
