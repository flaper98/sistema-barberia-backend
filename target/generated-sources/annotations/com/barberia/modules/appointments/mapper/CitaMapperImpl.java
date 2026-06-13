package com.barberia.modules.appointments.mapper;

import com.barberia.modules.appointments.dto.CitaResponse;
import com.barberia.modules.appointments.entity.Cita;
import com.barberia.modules.appointments.entity.CitaServicio;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-12T18:25:08-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class CitaMapperImpl implements CitaMapper {

    @Override
    public CitaResponse toResponse(Cita cita) {
        if ( cita == null ) {
            return null;
        }

        CitaResponse.CitaResponseBuilder citaResponse = CitaResponse.builder();

        citaResponse.servicios( citaServicioListToCitaServicioItemResponseList( cita.getServicios() ) );
        citaResponse.id( cita.getId() );
        citaResponse.clienteId( cita.getClienteId() );
        citaResponse.clienteNombre( cita.getClienteNombre() );
        citaResponse.barberoId( cita.getBarberoId() );
        citaResponse.barberoNombre( cita.getBarberoNombre() );
        citaResponse.fecha( cita.getFecha() );
        citaResponse.hora( cita.getHora() );
        citaResponse.totalEstimado( cita.getTotalEstimado() );
        citaResponse.notas( cita.getNotas() );
        citaResponse.ventaId( cita.getVentaId() );
        citaResponse.createdAt( cita.getCreatedAt() );
        citaResponse.updatedAt( cita.getUpdatedAt() );

        citaResponse.estado( cita.getEstado().name() );

        return citaResponse.build();
    }

    @Override
    public CitaResponse.CitaServicioItemResponse toServicioResponse(CitaServicio cs) {
        if ( cs == null ) {
            return null;
        }

        CitaResponse.CitaServicioItemResponse.CitaServicioItemResponseBuilder citaServicioItemResponse = CitaResponse.CitaServicioItemResponse.builder();

        citaServicioItemResponse.id( cs.getId() );
        citaServicioItemResponse.servicioId( cs.getServicioId() );
        citaServicioItemResponse.servicioNombre( cs.getServicioNombre() );
        citaServicioItemResponse.precio( cs.getPrecio() );
        citaServicioItemResponse.duracionMinutos( cs.getDuracionMinutos() );

        return citaServicioItemResponse.build();
    }

    protected List<CitaResponse.CitaServicioItemResponse> citaServicioListToCitaServicioItemResponseList(List<CitaServicio> list) {
        if ( list == null ) {
            return null;
        }

        List<CitaResponse.CitaServicioItemResponse> list1 = new ArrayList<CitaResponse.CitaServicioItemResponse>( list.size() );
        for ( CitaServicio citaServicio : list ) {
            list1.add( toServicioResponse( citaServicio ) );
        }

        return list1;
    }
}
