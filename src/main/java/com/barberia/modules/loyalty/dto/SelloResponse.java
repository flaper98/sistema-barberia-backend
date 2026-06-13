package com.barberia.modules.loyalty.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SelloResponse {

    private final Integer sellosActuales;
    private final Integer sellosNecesarios;
    private final Integer recompensasDisponibles;
    private final boolean nuevaRecompensaObtenida;
    private final String mensaje;
}
