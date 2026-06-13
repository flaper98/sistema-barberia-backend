package com.barberia.modules.barbershop.service.impl;

import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.barbershop.dto.ConfiguracionBarberiaRequest;
import com.barberia.modules.barbershop.dto.ConfiguracionBarberiaResponse;
import com.barberia.modules.barbershop.entity.ConfiguracionBarberia;
import com.barberia.modules.barbershop.mapper.ConfiguracionBarberiaMapper;
import com.barberia.modules.barbershop.repository.ConfiguracionBarberiaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfiguracionBarberiaServiceImpl {

    private static final int SINGLETON_ID = 1;

    private final ConfiguracionBarberiaRepository configRepo;
    private final ConfiguracionBarberiaMapper configMapper;

    @Transactional(readOnly = true)
    public ConfiguracionBarberiaResponse obtener() {
        return configMapper.toResponse(findOrThrow());
    }

    @Transactional
    public ConfiguracionBarberiaResponse actualizar(ConfiguracionBarberiaRequest request) {
        ConfiguracionBarberia config = findOrThrow();

        config.setNombre(request.getNombre());
        config.setDireccion(request.getDireccion());
        config.setTelefono(request.getTelefono());
        config.setEmail(request.getEmail());
        config.setRuc(request.getRuc());
        config.setHorario(request.getHorario());
        config.setLogoUrl(request.getLogoUrl());

        config = configRepo.save(config);
        log.info("Configuración de barbería actualizada");
        return configMapper.toResponse(config);
    }

    private ConfiguracionBarberia findOrThrow() {
        return configRepo.findById(SINGLETON_ID)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Configuración de barbería no encontrada. Ejecuta el script de datos semilla."));
    }
}
