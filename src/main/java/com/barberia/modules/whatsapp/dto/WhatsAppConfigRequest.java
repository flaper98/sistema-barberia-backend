package com.barberia.modules.whatsapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WhatsAppConfigRequest {

    private String phoneNumberId;

    /** Opcional: si viene vacío/null se conserva el token ya guardado (no se pisa). */
    private String accessToken;

    private String templateName;

    private String languageCode;

    private String graphApiVersion;

    @NotNull
    private Boolean enabled;
}
