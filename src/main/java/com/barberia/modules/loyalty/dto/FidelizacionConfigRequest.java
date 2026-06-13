package com.barberia.modules.loyalty.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FidelizacionConfigRequest {

    @NotNull
    @Min(1)
    private Integer sellosNecesarios;

    @NotNull
    @Min(1)
    private Integer sellosPorGanancia;

    private String descripcionRecompensa;

    @NotNull
    private Boolean activo;
}
