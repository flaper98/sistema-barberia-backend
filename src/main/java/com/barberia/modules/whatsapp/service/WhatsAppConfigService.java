package com.barberia.modules.whatsapp.service;

import com.barberia.common.CryptoService;
import com.barberia.exception.BusinessException;
import com.barberia.modules.whatsapp.dto.WhatsAppConfigRequest;
import com.barberia.modules.whatsapp.dto.WhatsAppConfigResponse;
import com.barberia.modules.whatsapp.entity.WhatsAppConfig;
import com.barberia.modules.whatsapp.repository.WhatsAppConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WhatsAppConfigService {

    private static final int SINGLETON_ID = 1;

    private final WhatsAppConfigRepository repository;
    private final CryptoService cryptoService;

    @Transactional(readOnly = true)
    public WhatsAppConfigResponse obtener() {
        WhatsAppConfig config = obtenerEntidad();
        return aResponse(config);
    }

    @Transactional
    public WhatsAppConfigResponse actualizar(WhatsAppConfigRequest request) {
        if (Boolean.TRUE.equals(request.getEnabled()) && !cryptoService.claveConfigurada()) {
            throw new BusinessException(
                    "No se puede activar el envío automático: falta configurar APP_ENCRYPTION_KEY en el servidor.");
        }

        WhatsAppConfig config = obtenerEntidad();
        config.setPhoneNumberId(request.getPhoneNumberId());
        config.setTemplateName(request.getTemplateName());
        config.setLanguageCode(StringUtils.hasText(request.getLanguageCode()) ? request.getLanguageCode() : "es");
        config.setGraphApiVersion(StringUtils.hasText(request.getGraphApiVersion()) ? request.getGraphApiVersion() : "v21.0");
        if (StringUtils.hasText(request.getAccessToken())) {
            config.setAccessTokenEncrypted(cryptoService.encriptar(request.getAccessToken().trim()));
        }
        config.setEnabled(request.getEnabled());
        config = repository.save(config);

        return aResponse(config);
    }

    @Transactional(readOnly = true)
    public boolean envioActivo() {
        WhatsAppConfig config = obtenerEntidad();
        return Boolean.TRUE.equals(config.getEnabled())
                && StringUtils.hasText(config.getPhoneNumberId())
                && StringUtils.hasText(config.getTemplateName())
                && StringUtils.hasText(config.getAccessTokenEncrypted());
    }

    /** Solo para uso interno del cliente de WhatsApp — nunca expuesto por un controller. */
    @Transactional(readOnly = true)
    public Optional<String> obtenerAccessTokenDescifrado() {
        WhatsAppConfig config = obtenerEntidad();
        if (!StringUtils.hasText(config.getAccessTokenEncrypted())) return Optional.empty();
        return Optional.of(cryptoService.desencriptar(config.getAccessTokenEncrypted()));
    }

    @Transactional(readOnly = true)
    public WhatsAppConfig obtenerConfigParaEnvio() {
        return obtenerEntidad();
    }

    private WhatsAppConfigResponse aResponse(WhatsAppConfig config) {
        return WhatsAppConfigResponse.builder()
                .phoneNumberId(config.getPhoneNumberId())
                .accessTokenConfigured(StringUtils.hasText(config.getAccessTokenEncrypted()))
                .templateName(config.getTemplateName())
                .languageCode(config.getLanguageCode())
                .graphApiVersion(config.getGraphApiVersion())
                .enabled(Boolean.TRUE.equals(config.getEnabled()))
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    private WhatsAppConfig obtenerEntidad() {
        return repository.findById(SINGLETON_ID)
                .orElseGet(() -> repository.save(WhatsAppConfig.builder().id(SINGLETON_ID).enabled(false).build()));
    }
}
