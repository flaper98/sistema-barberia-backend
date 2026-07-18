package com.barberia.modules.icloud.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IcloudConfigRequest {

    private String username;

    /** Opcional: si viene vacío/null se conserva la contraseña ya guardada (no se pisa). */
    private String appPassword;

    @NotNull
    private Boolean enabled;
}
