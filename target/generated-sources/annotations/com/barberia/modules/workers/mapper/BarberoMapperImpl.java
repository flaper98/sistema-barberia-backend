package com.barberia.modules.workers.mapper;

import com.barberia.modules.workers.dto.BarberoResponse;
import com.barberia.modules.workers.entity.Barbero;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-12T18:25:09-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class BarberoMapperImpl implements BarberoMapper {

    @Override
    public BarberoResponse toResponse(Barbero barbero) {
        if ( barbero == null ) {
            return null;
        }

        BarberoResponse.BarberoResponseBuilder barberoResponse = BarberoResponse.builder();

        barberoResponse.id( barbero.getId() );
        barberoResponse.nombre( barbero.getNombre() );
        barberoResponse.apellido( barbero.getApellido() );
        barberoResponse.telefono( barbero.getTelefono() );
        barberoResponse.especialidad( barbero.getEspecialidad() );
        barberoResponse.porcentajeComision( barbero.getPorcentajeComision() );
        barberoResponse.totalGenerado( barbero.getTotalGenerado() );
        barberoResponse.serviciosRealizados( barbero.getServiciosRealizados() );
        barberoResponse.estado( barbero.getEstado() );
        barberoResponse.avatar( barbero.getAvatar() );
        barberoResponse.createdAt( barbero.getCreatedAt() );
        barberoResponse.updatedAt( barbero.getUpdatedAt() );

        barberoResponse.nombreCompleto( barbero.getNombreCompleto() );

        return barberoResponse.build();
    }
}
