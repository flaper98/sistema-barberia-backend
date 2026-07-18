package com.barberia.modules.google.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleConfigRequest {

    private String username;

    /** Opcional: si viene vacío/null se conserva la contraseña ya guardada (no se pisa). */
    private String appPassword;

    @NotNull
    private Boolean enabled;
}
