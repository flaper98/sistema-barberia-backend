package com.barberia.modules.barbershop.mapper;

import com.barberia.modules.barbershop.dto.ConfiguracionBarberiaResponse;
import com.barberia.modules.barbershop.entity.ConfiguracionBarberia;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-12T18:25:08-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class ConfiguracionBarberiaMapperImpl implements ConfiguracionBarberiaMapper {

    @Override
    public ConfiguracionBarberiaResponse toResponse(ConfiguracionBarberia config) {
        if ( config == null ) {
            return null;
        }

        ConfiguracionBarberiaResponse.ConfiguracionBarberiaResponseBuilder configuracionBarberiaResponse = ConfiguracionBarberiaResponse.builder();

        configuracionBarberiaResponse.nombre( config.getNombre() );
        configuracionBarberiaResponse.direccion( config.getDireccion() );
        configuracionBarberiaResponse.telefono( config.getTelefono() );
        configuracionBarberiaResponse.email( config.getEmail() );
        configuracionBarberiaResponse.ruc( config.getRuc() );
        configuracionBarberiaResponse.horario( config.getHorario() );
        configuracionBarberiaResponse.logoUrl( config.getLogoUrl() );
        configuracionBarberiaResponse.updatedAt( config.getUpdatedAt() );

        return configuracionBarberiaResponse.build();
    }
}
