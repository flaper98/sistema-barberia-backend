package com.barberia.modules.services.mapper;

import com.barberia.modules.services.dto.ServicioResponse;
import com.barberia.modules.services.entity.Servicio;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-12T18:25:09-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class ServicioMapperImpl implements ServicioMapper {

    @Override
    public ServicioResponse toResponse(Servicio servicio) {
        if ( servicio == null ) {
            return null;
        }

        ServicioResponse.ServicioResponseBuilder servicioResponse = ServicioResponse.builder();

        servicioResponse.id( servicio.getId() );
        servicioResponse.nombre( servicio.getNombre() );
        servicioResponse.precio( servicio.getPrecio() );
        servicioResponse.duracionMinutos( servicio.getDuracionMinutos() );
        servicioResponse.descripcion( servicio.getDescripcion() );
        servicioResponse.estado( servicio.getEstado() );
        servicioResponse.createdAt( servicio.getCreatedAt() );
        servicioResponse.updatedAt( servicio.getUpdatedAt() );

        servicioResponse.categoria( servicio.getCategoria().name() );

        return servicioResponse.build();
    }
}
