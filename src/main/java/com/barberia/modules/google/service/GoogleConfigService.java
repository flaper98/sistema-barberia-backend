package com.barberia.modules.google.service;

import com.barberia.common.CryptoService;
import com.barberia.exception.BusinessException;
import com.barberia.modules.google.dto.GoogleConfigRequest;
import com.barberia.modules.google.dto.GoogleConfigResponse;
import com.barberia.modules.google.entity.GoogleConfig;
import com.barberia.modules.google.repository.GoogleConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleConfigService {

    private static final int SINGLETON_ID = 1;

    private final GoogleConfigRepository repository;
    private final CryptoService cryptoService;

    @Transactional(readOnly = true)
    public GoogleConfigResponse obtener() {
        GoogleConfig config = obtenerEntidad();
        return GoogleConfigResponse.builder()
                .username(config.getUsername())
                .passwordConfigured(config.getAppPasswordEncrypted() != null && !config.getAppPasswordEncrypted().isBlank())
                .enabled(Boolean.TRUE.equals(config.getEnabled()))
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    @Transactional
    public GoogleConfigResponse actualizar(GoogleConfigRequest request) {
        if (Boolean.TRUE.equals(request.getEnabled()) && !cryptoService.claveConfigurada()) {
            throw new BusinessException(
                    "No se puede activar la sincronización: falta configurar la clave de cifrado en el servidor.");
        }

        GoogleConfig config = obtenerEntidad();
        config.setUsername(request.getUsername());
        if (request.getAppPassword() != null && !request.getAppPassword().isBlank()) {
            config.setAppPasswordEncrypted(cryptoService.encriptar(request.getAppPassword()));
            config.setAddressbookHomeUrl(null); // credenciales nuevas -> hay que redescubrir
        }
        config.setEnabled(request.getEnabled());
        config = repository.save(config);

        return GoogleConfigResponse.builder()
                .username(config.getUsername())
                .passwordConfigured(config.getAppPasswordEncrypted() != null && !config.getAppPasswordEncrypted().isBlank())
                .enabled(Boolean.TRUE.equals(config.getEnabled()))
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    /** Solo para uso interno del cliente CardDAV — nunca expuesto por un controller. */
    @Transactional(readOnly = true)
    public Optional<String> obtenerAppPasswordDescifrada() {
        GoogleConfig config = obtenerEntidad();
        if (config.getAppPasswordEncrypted() == null || config.getAppPasswordEncrypted().isBlank()) {
            return Optional.empty();
        }
        return Optional.of(cryptoService.desencriptar(config.getAppPasswordEncrypted()));
    }

    @Transactional(readOnly = true)
    public boolean sincronizacionActiva() {
        GoogleConfig config = obtenerEntidad();
        return Boolean.TRUE.equals(config.getEnabled())
                && config.getUsername() != null && !config.getUsername().isBlank()
                && config.getAppPasswordEncrypted() != null && !config.getAppPasswordEncrypted().isBlank();
    }

    @Transactional(readOnly = true)
    public String obtenerUsername() {
        return obtenerEntidad().getUsername();
    }

    @Transactional(readOnly = true)
    public Optional<String> obtenerAddressbookHomeCacheado() {
        return Optional.ofNullable(obtenerEntidad().getAddressbookHomeUrl());
    }

    @Transactional
    public void guardarAddressbookHomeCacheado(String url) {
        GoogleConfig config = obtenerEntidad();
        config.setAddressbookHomeUrl(url);
        repository.save(config);
    }

    private GoogleConfig obtenerEntidad() {
        return repository.findById(SINGLETON_ID)
                .orElseGet(() -> repository.save(GoogleConfig.builder().id(SINGLETON_ID).enabled(false).build()));
    }
}
