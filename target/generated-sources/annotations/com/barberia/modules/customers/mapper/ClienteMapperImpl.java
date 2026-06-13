package com.barberia.modules.customers.mapper;

import com.barberia.modules.customers.dto.ClienteResponse;
import com.barberia.modules.customers.entity.Cliente;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-12T18:25:08-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class ClienteMapperImpl implements ClienteMapper {

    @Override
    public ClienteResponse toResponse(Cliente cliente) {
        if ( cliente == null ) {
            return null;
        }

        ClienteResponse.ClienteResponseBuilder clienteResponse = ClienteResponse.builder();

        clienteResponse.id( cliente.getId() );
        clienteResponse.nombre( cliente.getNombre() );
        clienteResponse.apellido( cliente.getApellido() );
        clienteResponse.correo( cliente.getCorreo() );
        clienteResponse.telefono( cliente.getTelefono() );
        clienteResponse.fechaNacimiento( cliente.getFechaNacimiento() );
        clienteResponse.notas( cliente.getNotas() );
        clienteResponse.estado( cliente.getEstado() );
        clienteResponse.cantidadSellos( cliente.getCantidadSellos() );
        clienteResponse.recompensasDisponibles( cliente.getRecompensasDisponibles() );
        clienteResponse.ultimaVisita( cliente.getUltimaVisita() );
        clienteResponse.createdAt( cliente.getCreatedAt() );
        clienteResponse.updatedAt( cliente.getUpdatedAt() );

        clienteResponse.nombreCompleto( cliente.getNombreCompleto() );

        return clienteResponse.build();
    }
}
