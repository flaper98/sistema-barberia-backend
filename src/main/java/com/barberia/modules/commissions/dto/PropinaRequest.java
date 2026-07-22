package com.barberia.modules.commissions.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PropinaRequest {

    @NotNull(message = "El barbero es obligatorio")
    private Integer barberoId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    // Si no se manda, se usa la fecha de hoy.
    private LocalDate fecha;

    @Size(max = 255, message = "Las notas no pueden superar 255 caracteres")
    private String notas;
}
