package com.barberia.modules.barbershop.service.impl;

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
    private static final String MENSAJE_BIENVENIDA_POR_DEFECTO =
            "¡Hola {nombre}! 👋 Bienvenido/a a {negocio}. Gracias por registrarte con nosotros, "
                    + "cualquier consulta escribinos por acá. ¡Nos vemos pronto! ✂️";
    private static final String MENSAJE_RECORDATORIO_POR_DEFECTO =
            "¡Hola {nombre}! 👋 Te extrañamos en {negocio}. Tenemos promociones especiales esperándote, "
                    + "¡vení a darte un corte! ✂️";

    private final ConfiguracionBarberiaRepository configRepo;
    private final ConfiguracionBarberiaMapper configMapper;

    @Transactional
    public ConfiguracionBarberiaResponse obtener() {
        return configMapper.toResponse(findOrCrearPorDefecto());
    }

    @Transactional
    public ConfiguracionBarberiaResponse actualizar(ConfiguracionBarberiaRequest request) {
        ConfiguracionBarberia config = findOrCrearPorDefecto();

        config.setNombre(request.getNombre());
        config.setDireccion(request.getDireccion());
        config.setTelefono(request.getTelefono());
        config.setEmail(request.getEmail());
        config.setRuc(request.getRuc());
        config.setHorario(request.getHorario());
        config.setLogoUrl(request.getLogoUrl());
        config.setMensajeBienvenidaWhatsapp(request.getMensajeBienvenidaWhatsapp());
        config.setMensajeRecordatorioWhatsapp(request.getMensajeRecordatorioWhatsapp());

        config = configRepo.save(config);
        log.info("Configuración de barbería actualizada");
        return configMapper.toResponse(config);
    }

    /** Antes esto lanzaba 404 si nadie había corrido un script de datos semilla; ahora se autocompleta una vez. */
    private ConfiguracionBarberia findOrCrearPorDefecto() {
        return configRepo.findById(SINGLETON_ID).orElseGet(() -> configRepo.save(
                ConfiguracionBarberia.builder()
                        .id(SINGLETON_ID)
                        .nombre("Árabes Barber Studio")
                        .mensajeBienvenidaWhatsapp(MENSAJE_BIENVENIDA_POR_DEFECTO)
                        .mensajeRecordatorioWhatsapp(MENSAJE_RECORDATORIO_POR_DEFECTO)
                        .build()));
    }
}
