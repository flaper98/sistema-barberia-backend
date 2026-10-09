package com.barberia.modules.loyalty.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CanjearRecompensaRequest {

    @NotNull(message = "Debe seleccionar el premio a entregar")
    private Integer recompensaId;

    @Size(max = 255, message = "El motivo no puede superar 255 caracteres")
    private String motivo;
}
