package com.barberia.modules.whatsapp.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** Nunca incluye el access token ni su forma cifrada — solo si ya hay uno guardado. */
@Getter
@Builder
public class WhatsAppConfigResponse {
    private final String phoneNumberId;
    private final boolean accessTokenConfigured;
    private final String templateName;
    private final String languageCode;
    private final String graphApiVersion;
    private final boolean enabled;
    private final LocalDateTime updatedAt;
}
