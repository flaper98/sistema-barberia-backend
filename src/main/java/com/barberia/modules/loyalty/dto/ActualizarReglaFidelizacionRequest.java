package com.barberia.modules.loyalty.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualizarReglaFidelizacionRequest {

    @NotNull(message = "aplicaFidelizacion es obligatorio")
    private Boolean aplicaFidelizacion;
}
