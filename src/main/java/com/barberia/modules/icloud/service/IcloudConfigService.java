package com.barberia.modules.icloud.service;

import com.barberia.common.CryptoService;
import com.barberia.exception.BusinessException;
import com.barberia.modules.icloud.dto.IcloudConfigRequest;
import com.barberia.modules.icloud.dto.IcloudConfigResponse;
import com.barberia.modules.icloud.entity.IcloudConfig;
import com.barberia.modules.icloud.repository.IcloudConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IcloudConfigService {

    private static final int SINGLETON_ID = 1;

    private final IcloudConfigRepository repository;
    private final CryptoService cryptoService;

    @Transactional(readOnly = true)
    public IcloudConfigResponse obtener() {
        IcloudConfig config = obtenerEntidad();
        return IcloudConfigResponse.builder()
                .username(config.getUsername())
                .passwordConfigured(config.getAppPasswordEncrypted() != null && !config.getAppPasswordEncrypted().isBlank())
                .enabled(Boolean.TRUE.equals(config.getEnabled()))
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    @Transactional
    public IcloudConfigResponse actualizar(IcloudConfigRequest request) {
        if (Boolean.TRUE.equals(request.getEnabled()) && !cryptoService.claveConfigurada()) {
            throw new BusinessException(
                    "No se puede activar la sincronización: falta configurar APP_ENCRYPTION_KEY en el servidor.");
        }

        IcloudConfig config = obtenerEntidad();
        config.setUsername(request.getUsername() != null ? request.getUsername().trim() : null);
        if (request.getAppPassword() != null && !request.getAppPassword().isBlank()) {
            config.setAppPasswordEncrypted(cryptoService.encriptar(request.getAppPassword().trim()));
            config.setAddressbookHomeUrl(null); // credenciales nuevas -> hay que redescubrir
        }
        config.setEnabled(request.getEnabled());
        config = repository.save(config);

        return IcloudConfigResponse.builder()
                .username(config.getUsername())
                .passwordConfigured(config.getAppPasswordEncrypted() != null && !config.getAppPasswordEncrypted().isBlank())
                .enabled(Boolean.TRUE.equals(config.getEnabled()))
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    /** Solo para uso interno del cliente CardDAV — nunca expuesto por un controller. */
    @Transactional(readOnly = true)
    public Optional<String> obtenerAppPasswordDescifrada() {
        IcloudConfig config = obtenerEntidad();
        if (config.getAppPasswordEncrypted() == null || config.getAppPasswordEncrypted().isBlank()) {
            return Optional.empty();
        }
        return Optional.of(cryptoService.desencriptar(config.getAppPasswordEncrypted()));
    }

    @Transactional(readOnly = true)
    public boolean sincronizacionActiva() {
        IcloudConfig config = obtenerEntidad();
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
        IcloudConfig config = obtenerEntidad();
        config.setAddressbookHomeUrl(url);
        repository.save(config);
    }

    private IcloudConfig obtenerEntidad() {
        return repository.findById(SINGLETON_ID)
                .orElseGet(() -> repository.save(IcloudConfig.builder().id(SINGLETON_ID).enabled(false).build()));
    }
}
