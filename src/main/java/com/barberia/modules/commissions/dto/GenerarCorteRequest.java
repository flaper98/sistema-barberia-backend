package com.barberia.modules.commissions.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GenerarCorteRequest {

    @NotNull(message = "El barbero es obligatorio")
    private Integer barberoId;

    @NotNull(message = "La fecha desde es obligatoria")
    private LocalDate fechaDesde;

    @NotNull(message = "La fecha hasta es obligatoria")
    private LocalDate fechaHasta;

    @Size(max = 500, message = "Las notas no pueden superar 500 caracteres")
    private String notas;
}
