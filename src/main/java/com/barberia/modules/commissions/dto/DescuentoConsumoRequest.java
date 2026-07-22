package com.barberia.modules.commissions.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DescuentoConsumoRequest {

    @NotNull(message = "El barbero es obligatorio")
    private Integer barberoId;

    @NotNull(message = "El producto es obligatorio")
    private Integer productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @Size(max = 255, message = "El motivo no puede superar 255 caracteres")
    private String motivo;
}
